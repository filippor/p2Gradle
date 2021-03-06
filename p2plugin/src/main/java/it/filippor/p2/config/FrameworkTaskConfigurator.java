package it.filippor.p2.config;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.tasks.TaskProvider;

import it.filippor.p2.api.Bundle;
import it.filippor.p2.api.P2RepositoryManager;
import it.filippor.p2.framework.FrameworkLauncher;
import it.filippor.p2.framework.ServiceProvider;
import it.filippor.p2.task.FileProviderTask;
import it.filippor.p2.task.PublishTask;
import it.filippor.p2.task.ResolveTask;
import it.filippor.p2.task.TaskWithProgress;
import it.filippor.p2.util.ProgressMonitorWrapper;

public class FrameworkTaskConfigurator {
  public static final String P2_FRAMEWORK_BUNDLES_CONFIG = "p2frameworkBundles";

  public static final String P2_START_FRAMEWORK_TASK = "p2startFramework";

  public static final String P2_STOP_FRAMEWORK_TASK = "p2stopFramework";

  private final Task stopFrameworkTask;

  private final Task startFrameworkTask;

  private final FrameworkLauncher p2FrameworkLauncher;

  private final Project project;

  private Collection<URI> updateSites;

  private URI agentUri;

  public FrameworkTaskConfigurator(final Project project, final URI agentUri) {
    this.agentUri = agentUri;
    this.updateSites = new ArrayList<>();
    final Project rootPrj = project.getRootProject();
    this.project = project;

   

    this.p2FrameworkLauncher = this.createFrameworkLauncher(rootPrj.getRootProject());

    Task stopFrameworkTask = rootPrj.getTasks().findByName(FrameworkTaskConfigurator.P2_STOP_FRAMEWORK_TASK);
    if (stopFrameworkTask == null) {
      stopFrameworkTask = rootPrj.getTasks()
        .register(FrameworkTaskConfigurator.P2_STOP_FRAMEWORK_TASK, TaskWithProgress.class, it -> it.doLast(task -> {
          if (this.p2FrameworkLauncher.isStarted()) {
            this.p2FrameworkLauncher.executeWithServiceProvider((ServiceProvider sp) -> {
              P2RepositoryManager repositoryManager = sp.getService(P2RepositoryManager.class);
              if (repositoryManager != null)
                repositoryManager.tearDown();
            });
            this.p2FrameworkLauncher.stopFramework();
          }
        }))
        .get();
    }
    this.stopFrameworkTask = stopFrameworkTask;

    Task startFrameworkTask = rootPrj.getTasks().findByName(FrameworkTaskConfigurator.P2_START_FRAMEWORK_TASK);
    if (startFrameworkTask == null) {
      startFrameworkTask = rootPrj.getTasks()
        .register(FrameworkTaskConfigurator.P2_START_FRAMEWORK_TASK, TaskWithProgress.class, t -> {
          t.finalizedBy(this.stopFrameworkTask);
          t.doLast(it -> {
            this.p2FrameworkLauncher.startFramework();
            this.p2FrameworkLauncher.executeWithServiceProvider((ServiceProvider sp) -> sp.getService(P2RepositoryManager.class)
              .init(getAgentUri(), getUpdateSites(), ProgressMonitorWrapper.wrap(t)));
          });
        })
        .get();
    }
    this.startFrameworkTask = startFrameworkTask;
  }

  public ConfigurableFileCollection bundles(final String... bundles) {
    return this.bundles(true, bundles);
  }

  public ConfigurableFileCollection bundles(final boolean transitive, final String... bundles) {
    return this.bundles(transitive,
                          Arrays.stream(bundles)
                            .map(s -> s.split(":"))
                            .map(sa -> new Bundle(sa[0], new org.osgi.framework.VersionRange(sa[1])))
                            .toArray(Bundle[]::new));
  }

  public ConfigurableFileCollection bundles(final Bundle... bundles) {
    return this.bundles(true, bundles);
  }

  public ConfigurableFileCollection bundles(final boolean transitive, final Bundle... bundles) {
    String nameQual = "";
    if (transitive) {
      nameQual = "transitive";
    }
    String       nameBundle    = Arrays.toString(bundles);
    final String filesTaskName = nameQual + nameBundle;
    final String name          = ("resolveP2" + filesTaskName);

    Task tmpResolve = this.project.getTasks().findByName(name);
    if (tmpResolve == null) {
      tmpResolve = this.project.getTasks().register(name, ResolveTask.class, it -> {
        it.p2FrameworkLauncher = this.p2FrameworkLauncher;
        it.bundles             = Arrays.asList(bundles);
        it.transitive          = transitive;
      }).get();
    }
    tmpResolve.getDependsOn().add(startFrameworkTask);
    final Task resolve = tmpResolve;

    this.stopFrameworkTask.mustRunAfter(stopFrameworkTask.getMustRunAfter(), resolve);

    Task filesTask = this.project.getTasks().findByName(filesTaskName);
    if (filesTask == null) {
      filesTask = project.getTasks()
        .register(filesTaskName, FileProviderTask.class, it -> it.setResolver((ResolveTask) resolve))
        .get();
    }
    return this.project.files(filesTask);
  }

  public TaskProvider<PublishTask> publishTask(final String name, final Action<PublishTask> action) {
    TaskProvider<PublishTask> publishTask = this.project.getTasks().register(name, PublishTask.class, action);
    publishTask.configure(it -> {
      it.getDependsOn().add(this.startFrameworkTask);
      it.p2FrameworkLauncher = this.p2FrameworkLauncher;
    });

    this.stopFrameworkTask.mustRunAfter(this.stopFrameworkTask.getMustRunAfter(), publishTask);

    return publishTask;
  }

  public FrameworkLauncher createFrameworkLauncher(final Project project) {
    Configuration bundles = project.getConfigurations().findByName(FrameworkTaskConfigurator.P2_FRAMEWORK_BUNDLES_CONFIG);
    if (bundles == null) {
      bundles = project.getConfigurations().create(FrameworkTaskConfigurator.P2_FRAMEWORK_BUNDLES_CONFIG);
      project.getDependencies().add(bundles.getName(), "it.filippor.p2:p2impl:0.0.1");
    }
    
    
    final File         frameworkStoragePath      = project.getBuildDir().toPath().resolve("tmp").resolve("p2Framework").toFile();
    final Set<String>  p2ApiPackage              = new HashSet<>(Collections.singletonList("it.filippor.p2.api"));
    final List<String> startBundlesSymbolicNames = Arrays.asList("org.eclipse.equinox.ds", "org.eclipse.equinox.registry",
                                                                 "org.eclipse.core.net", "org.apache.felix.scr", "p2impl");
    return new FrameworkLauncher(frameworkStoragePath, p2ApiPackage, startBundlesSymbolicNames, bundles);
  }

  public Task doLastOnFramework(final Task task, final BiConsumer<Task, ServiceProvider> action) {
    task.getDependsOn().add(this.startFrameworkTask);
    this.stopFrameworkTask.mustRunAfter(this.stopFrameworkTask.getMustRunAfter(), task);

    return task.doLast(t -> {
      if ((!this.p2FrameworkLauncher.isStarted())) {
        t.getLogger().warn("framework is not running");
        this.p2FrameworkLauncher.startFramework();
      }
      this.p2FrameworkLauncher.executeWithServiceProvider(it -> action.accept(t, it));
    });
  }

  public Collection<URI> getUpdateSites() {
    return updateSites;
  }

  public void setUpdateSites(Collection<URI> updateSites) {
    this.updateSites = updateSites;
  }

  public URI getAgentUri() {
    return agentUri;
  }

  public void setAgentUri(URI agentUri) {
    this.agentUri = agentUri;
  }
}
