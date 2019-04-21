package it.filippor.p2.config;

import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

import org.eclipse.osgi.service.resolver.VersionRange;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.ListExtensions;
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

  public FrameworkTaskConfigurator(final Project project, final URI agentUri, final Collection<URI> updateSites) {
    final Project prj = project.getRootProject();
    this.project = project;

    Configuration frameworkBundles = prj.getConfigurations().findByName(FrameworkTaskConfigurator.P2_FRAMEWORK_BUNDLES_CONFIG);
    if (frameworkBundles == null) {
      frameworkBundles = prj.getConfigurations().create(FrameworkTaskConfigurator.P2_FRAMEWORK_BUNDLES_CONFIG);
      prj.getDependencies().add(frameworkBundles.getName(), "it.filippor.p2:p2impl:0.0.1");
    }

    this.p2FrameworkLauncher = this.createFrameworkLauncher(prj, frameworkBundles);

    Task stopFrameworkTask = prj.getTasks().findByName(FrameworkTaskConfigurator.P2_STOP_FRAMEWORK_TASK);
    if (stopFrameworkTask == null) {
      stopFrameworkTask = prj.getTasks()
        .<TaskWithProgress> register(FrameworkTaskConfigurator.P2_STOP_FRAMEWORK_TASK, TaskWithProgress.class,
                                     (Action<TaskWithProgress>) (TaskWithProgress it) -> {
                                       it.doLast((Action<Task>) (Task task) -> {
                                         if (this.p2FrameworkLauncher.isStarted()) {
                                           this.p2FrameworkLauncher.executeWithServiceProvider((ServiceProvider sp) -> {
                                             P2RepositoryManager repositoryManager = sp.getService(P2RepositoryManager.class);
                                             if (repositoryManager != null)
                                               repositoryManager.tearDown();
                                           });
                                           this.p2FrameworkLauncher.stopFramework();
                                         }
                                       });
                                     })
        .get();
    }
    this.stopFrameworkTask = stopFrameworkTask;

    Task startFrameworkTask = prj.getTasks().findByName(FrameworkTaskConfigurator.P2_START_FRAMEWORK_TASK);
    if (startFrameworkTask == null) {
      startFrameworkTask = prj.getTasks()
        .<TaskWithProgress> register(FrameworkTaskConfigurator.P2_START_FRAMEWORK_TASK, TaskWithProgress.class,
                                     (Action<TaskWithProgress>) (TaskWithProgress t) -> {
                                       t.finalizedBy(this.stopFrameworkTask);
                                       t.doLast((Action<Task>) (Task it) -> {
                                         this.p2FrameworkLauncher.startFramework();
                                         this.p2FrameworkLauncher.executeWithServiceProvider((ServiceProvider sp) -> {
                                           P2RepositoryManager _service = sp.getService(P2RepositoryManager.class);
                                           ProgressMonitorWrapper _progressMonitorWrapper = new ProgressMonitorWrapper(t);
                                           _service.init(agentUri, updateSites, _progressMonitorWrapper);
                                         });
                                       });
                                     })
        .get();
    }
    this.startFrameworkTask = startFrameworkTask;
  }

  public ConfigurableFileCollection p2Bundles(final String... bundles) {
    return this.p2Bundles(true, bundles);
  }

  public ConfigurableFileCollection p2Bundles(final boolean transitive, final String... bundles) {

    return this.p2Bundles(transitive, ((Bundle[]) Conversions
      .unwrapArray((List<Bundle>) ListExtensions.<String[], Bundle> map(ListExtensions
        .<String, String[]> map(Arrays.<String> asList(bundles), (Function1<String, String[]>) (String it) -> it.split(":")),
                                                                        (Function1<String[], Bundle>) (String[] it) -> {
                                                                          return new Bundle(it[0], new VersionRange(it[1]));
                                                                        }),
                   Bundle.class)));
  }

  public ConfigurableFileCollection p2Bundles(final Bundle... bundles) {
    return this.p2Bundles(true, bundles);
  }

  public ConfigurableFileCollection p2Bundles(final boolean transitive, final Bundle... bundles) {
    String nameQual = "";
    if (transitive) {
      nameQual = "transitive";
    }
    String       nameBundle    = Arrays.toString(bundles);
    final String filesTaskName = (nameQual + nameBundle);
    final String name          = ("resolveP2" + filesTaskName);

    Task tmpResolve = this.project.getTasks().findByName(name);
    if (tmpResolve == null) {
      tmpResolve = this.project.getTasks()
        .<ResolveTask> register(name, ResolveTask.class, (Action<ResolveTask>) (ResolveTask it) -> {
          it.p2FrameworkLauncher = this.p2FrameworkLauncher;
          it.bundles           = Arrays.<Bundle> asList(bundles);
          it.transitive        = transitive;
        })
        .get();
    }
    tmpResolve.getDependsOn().add(this.startFrameworkTask);
    final Task resolve = tmpResolve;

    this.stopFrameworkTask.mustRunAfter(this.stopFrameworkTask.getMustRunAfter(), resolve);

    Task filesTask = this.project.getTasks().findByName(filesTaskName);
    if (filesTask == null) {
      final Action<FileProviderTask> _function_1 = (FileProviderTask it) -> {
                                                   it.setResolver(((ResolveTask) resolve));
                                                 };
      FileProviderTask               _get_1      = this.project.getTasks()
        .<FileProviderTask> register(filesTaskName, FileProviderTask.class, _function_1)
        .get();
      filesTask = _get_1;
    }
    return this.project.files(filesTask);
  }

  public TaskProvider<PublishTask> publishTask(final String name, final Action<PublishTask> action) {
    TaskProvider<PublishTask> publishTask = this.project.getTasks().<PublishTask> register(name, PublishTask.class, action);
    publishTask.configure((Action<PublishTask>) (PublishTask it) -> {
      it.getDependsOn().add(this.startFrameworkTask);
      it.p2FrameworkLauncher = this.p2FrameworkLauncher;
    });

    this.stopFrameworkTask.mustRunAfter(this.stopFrameworkTask.getMustRunAfter(), publishTask);

    return publishTask;
  }

  public FrameworkLauncher createFrameworkLauncher(final Project project, final Iterable<File> bundles) {
    final File         frameworkStoragePath      = project.getBuildDir().toPath().resolve("tmp").resolve("p2Framework").toFile();
    final Set<String>  p2ApiPackage              = Collections
      .unmodifiableSet(CollectionLiterals.newHashSet("it.filippor.p2.api"));
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
      this.p2FrameworkLauncher.executeWithServiceProvider((ServiceProvider it) -> {
        action.accept(t, it);
      });
    });
  }
}
