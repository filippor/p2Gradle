package it.filippor.p2.config;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
import it.filippor.p2.task.PublishTask;
import it.filippor.p2.task.ResolveTask;
import it.filippor.p2.task.TaskWithProgress;
import it.filippor.p2.util.ProgressMonitorWrapper;

/**
 * P2 Plugin Configuration
 * 
 * @author filippo.rossoni
 */
public class FrameworkTaskConfigurator {
	private static final String IT_FILIPPOR_P2_P2IMPL = "it.filippor.p2:p2impl:0.0.6";

	private static final String P2_FRAMEWORK_BUNDLES_CONFIG = "p2frameworkBundles";

	private static final String P2_START_FRAMEWORK_TASK = "p2startFramework";

	private static final String P2_STOP_FRAMEWORK_TASK = "p2stopFramework";

	private final Task stopFrameworkTask;

	private final Task startFrameworkTask;

	private final FrameworkLauncher p2FrameworkLauncher;

	private Set<String> sharedPackages = Set.of("it.filippor.p2.api");

	private final Project project;

	private Collection<URI> updateSites;

	private Path bundleCache;// = Paths.get(System.getProperty("user.home"), ".gradle", "caches", "p2",
								// "pool");

	private List<String> startBundlesSymbolicNames = Arrays.asList("org.eclipse.equinox.ds",
			"org.eclipse.equinox.registry", "org.eclipse.core.net", "org.apache.felix.scr", "p2impl");

	private Map<String, String> defaultTargetProperties;

	/**
	 * get ftramework launcher
	 * 
	 * @return the framework launcher
	 */
	public FrameworkLauncher getP2FrameworkLauncher() {
		return p2FrameworkLauncher;
	}

	/**
	 * getBundleCache
	 * 
	 * @return the bundleCache path
	 */
	public Path getBundleCache() {
		return bundleCache;
	}

	/**
	 * set path to cache for bundles
	 * 
	 * @param bundleCache the bundleCache path to set
	 */
	public void setBundleCache(Path bundleCache) {
		this.bundleCache = bundleCache;
	}

	/**
	 * getStartBundlesSymbolicNames
	 * 
	 * @return list of bundle symbolic name to start
	 */
	public List<String> getStartBundlesSymbolicNames() {
		return startBundlesSymbolicNames;
	}

	/**
	 * setStartBundlesSymbolicNames
	 * 
	 * @param startBundlesSymbolicNames list of bundle symbolic name to start
	 */
	public void setStartBundlesSymbolicNames(List<String> startBundlesSymbolicNames) {
		this.startBundlesSymbolicNames = startBundlesSymbolicNames;
	}

	/**
	 * geSharedPackages
	 * 
	 * @return package shared between framework and gradle
	 */
	public Set<String> geSharedPackages() {
		return sharedPackages;
	}

	/**
	 * setSharedPackages
	 * 
	 * @param sharedPackages package shared between framework and gradle
	 */
	public void setSharedPackages(Set<String> sharedPackages) {
		this.sharedPackages = sharedPackages;
	}

	/**
	 * create configuration
	 *
	 * @param project     project to configure
	 * @param bundleCache bundle cache path
	 */
	public FrameworkTaskConfigurator(final Project project, final Path bundleCache) {
		this.bundleCache = bundleCache;
		this.updateSites = new ArrayList<>();
		final Project rootPrj = project.getRootProject();
		this.project = project;

		this.p2FrameworkLauncher = this.createFrameworkLauncher(rootPrj.getRootProject());

		Task stopFrameworkTask = rootPrj.getTasks().findByName(FrameworkTaskConfigurator.P2_STOP_FRAMEWORK_TASK);
		if (stopFrameworkTask == null) {
			stopFrameworkTask = rootPrj.getTasks().register(FrameworkTaskConfigurator.P2_STOP_FRAMEWORK_TASK,
					TaskWithProgress.class, it -> it.doLast(task -> {
						if (this.p2FrameworkLauncher.isStarted()) {
							this.p2FrameworkLauncher.executeWithServiceProvider((ServiceProvider sp) -> {
								P2RepositoryManager repositoryManager = sp.getService(P2RepositoryManager.class);
								if (repositoryManager != null)
									repositoryManager.tearDown();
							});
							this.p2FrameworkLauncher.stopFramework();
						}
					})).get();
		}
		this.stopFrameworkTask = stopFrameworkTask;

		Task startFrameworkTask = rootPrj.getTasks().findByName(FrameworkTaskConfigurator.P2_START_FRAMEWORK_TASK);
		if (startFrameworkTask == null) {
			startFrameworkTask = rootPrj.getTasks()
					.register(FrameworkTaskConfigurator.P2_START_FRAMEWORK_TASK, TaskWithProgress.class, t -> {
						t.finalizedBy(this.stopFrameworkTask);
						t.doLast(it -> {
							this.p2FrameworkLauncher.startFramework();
							this.p2FrameworkLauncher.executeWithServiceProvider((ServiceProvider sp) -> {
								sp.getService(P2RepositoryManager.class).init(
										this.project.getLayout().getBuildDirectory().dir("p2").get().dir("p2Agent").getAsFile().toURI(),
										this.updateSites, ProgressMonitorWrapper.wrap(t));
							});
						});
					}).get();
		} else {
			startFrameworkTask.finalizedBy(stopFrameworkTask);
		}
		startFrameworkTask.getDestroyables().register(stopFrameworkTask);

		this.startFrameworkTask = startFrameworkTask;
	}

	/**
	 * create a file collection with bundles resolved from bundles specification
	 * including transitive dependencies
	 *
	 * @param bundles bundles to resolve
	 *
	 * @return resolved artifacts
	 */
	public ConfigurableFileCollection bundles(final String... bundles) {
		return this.bundles(true, bundles);
	}

	/**
	 * create a file collection with bundles resolved from bundles specification
	 * including transitive dependencies if transitive is true
	 *
	 * @param transitive include transitive dependencies
	 * @param bundles    bundles to resolve
	 *
	 * @return resolved artifacts
	 */
	public ConfigurableFileCollection bundles(final boolean transitive, final String... bundles) {
		return this.bundles(transitive,
				Arrays.stream(bundles).map(s -> s.split(":"))
						.map(sa -> new Bundle(sa[0], sa.length > 1 ? new org.osgi.framework.VersionRange(sa[1]) : null))
						.toArray(Bundle[]::new));
	}

	/**
	 * resolve bundles
	 * @param transitive       include transitive dependencies
	 * @param targetProperties artifact repository properties for resolution
	 * @param bundles          bundles bundles to resolve
	 *
	 * @return resolved artifacts
	 */
	public ConfigurableFileCollection bundles(final boolean transitive, Map<String, String> targetProperties,
			final String... bundles) {
		return this.bundles(transitive, targetProperties,
				Arrays.stream(bundles).map(s -> s.split(":"))
						.map(sa -> new Bundle(sa[0], sa.length > 1 ? new org.osgi.framework.VersionRange(sa[1]) : null))
						.toArray(Bundle[]::new));
	}

	/**
	 * create a file collection with bundles resolved from bundles specification
	 * including transitive dependencies
	 *
	 * @param bundles bundles bundles to resolve
	 *
	 * @return resolved atifacts
	 */
	public ConfigurableFileCollection bundles(final Bundle... bundles) {
		return this.bundles(true, bundles);
	}

	/**
	 * create a file collection with bundles resolved from bundles specification
	 * including transitive dependencies if transitive is true
	 *
	 * @param transitive include transitive dependencies
	 * @param bundles    bundles bundles to resolve
	 *
	 * @return resolved atifacts
	 */
	public ConfigurableFileCollection bundles(final boolean transitive, final Bundle... bundles) {
		return bundles(transitive, defaultTargetProperties, bundles);
	}

	/**
	 * create a file collection with bundles resolved from bundles specification
	 * including transitive dependencies if transitive is true
	 *
	 * @param transitive       include transitive dependencies
	 * @param targetProperties artifact repository properties for resolution
	 * @param bundles          bundles bundles to resolve
	 *
	 * @return resolved atifacts
	 */
	public ConfigurableFileCollection bundles(final boolean transitive, Map<String, String> targetProperties,
			final Bundle... bundles) {
		String nameQual = "";
		if (transitive) {
			nameQual = "transitive";
		}
		String nameBundle = "" + Objects.hash((Object[]) bundles) + targetProperties.hashCode();
		final String filesTaskName = nameQual + nameBundle;
		final String name = ("resolveP2" + filesTaskName);
		ResolveTask tmpResolve = (ResolveTask) this.project.getTasks().findByName(name);
		if (tmpResolve == null) {
			tmpResolve = this.project.getTasks().register(name, ResolveTask.class, it -> {
				it.p2FrameworkLauncher = this.p2FrameworkLauncher;
				it.bundles = Arrays.asList(bundles);
				it.transitive = transitive;
				it.targetProperties = targetProperties;
			}).get();
		}
		tmpResolve.getDependsOn().add(startFrameworkTask);
		final ResolveTask resolve = tmpResolve;

		this.stopFrameworkTask.mustRunAfter(stopFrameworkTask.getMustRunAfter(), resolve);

		return this.project.files(resolve);
	}

	/**
	 * add task to publish a p2 repository
	 *
	 * @param name   name of new task
	 * @param action action to configure task
	 *
	 * @return created task
	 */
	public TaskProvider<PublishTask> publishTask(final String name, final Action<PublishTask> action) {
		TaskProvider<PublishTask> publishTask = this.project.getTasks().register(name, PublishTask.class, action);
		publishTask.configure(it -> {
			it.getDependsOn().add(this.startFrameworkTask);
			it.p2FrameworkLauncher = this.p2FrameworkLauncher;
		});

		this.stopFrameworkTask.mustRunAfter(this.stopFrameworkTask.getMustRunAfter(), publishTask);

		return publishTask;
	}

	private FrameworkLauncher createFrameworkLauncher(final Project project) {
		Configuration bundles = project.getConfigurations()
				.findByName(FrameworkTaskConfigurator.P2_FRAMEWORK_BUNDLES_CONFIG);
		if (bundles == null) {
			bundles = project.getRootProject().getConfigurations()
					.create(FrameworkTaskConfigurator.P2_FRAMEWORK_BUNDLES_CONFIG);
			project.getRootProject().getDependencies().add(FrameworkTaskConfigurator.P2_FRAMEWORK_BUNDLES_CONFIG,
					IT_FILIPPOR_P2_P2IMPL);
		}
		final File frameworkStoragePath = this.project.getLayout().getBuildDirectory().dir("p2").get().dir("p2Framework").getAsFile();

		return new FrameworkLauncher(frameworkStoragePath, sharedPackages, startBundlesSymbolicNames, bundles);
	}

	/**
	 * add doLast action that run with framework activated and serviceProvider
	 *
	 * @param taskProvider task to configure
	 * @param action       task configuration
	 *
	 * @return task
	 */
	public Task doLastOnFramework(final TaskProvider<?> taskProvider, final BiConsumer<Task, ServiceProvider> action) {
		return doLastOnFramework(taskProvider.get(), action);
	}

	/**
	 * add doLast action that run with framework activated and serviceProvider
	 *
	 * @param task   task to configure
	 * @param action task configuration
	 *
	 * @return task
	 */
	private Task doLastOnFramework(Task task, final BiConsumer<Task, ServiceProvider> action) {
		task.getDependsOn().add(this.startFrameworkTask);
		this.stopFrameworkTask.mustRunAfter(this.stopFrameworkTask.getMustRunAfter(), task);

		return task.doLast(t -> {
			if ((!this.p2FrameworkLauncher.isStarted())) {
				t.getLogger().warn("framework is not running");
				this.p2FrameworkLauncher.startFramework();
			}
			if (action != null)
				this.p2FrameworkLauncher.executeWithServiceProvider(it -> action.accept(t, it));
		});
	}

	/**
	 * getUpdateSites
	 * @return configured update site
	 */
	public Collection<URI> getUpdateSites() {
		return updateSites;
	}

	/**
	 * setUpdateSites
	 * @param updateSites update site to check when resolve
	 */
	public void setUpdateSites(Collection<URI> updateSites) {
		this.updateSites = updateSites;
	}

	/**
	 * getDefaultTargetProperties
	 * @return targetProperties used for dependency that not specify one
	 */
	public Map<String, String> getDefaultTargetProperties() {
		return defaultTargetProperties;
	}

	/**
	 * getBaseTargetProperties
	 * @return the base properties required to lauch the framework
	 */
	protected Map<String, String> getBaseTargetProperties() {
		Map<String, String> props = new HashMap<>();
		props.put("org.eclipse.equinox.p2.roaming", "true");
		props.put("org.eclipse.equinox.p2.cache", bundleCache.toString());
		return props;
	}

	/**
	 * setDefaultTargetProperties
	 * @param defaultTargetProperties targetProperties used for dependency that not
	 *                                specify one
	 */
	public void setDefaultTargetProperties(Map<String, String> defaultTargetProperties) {

		this.defaultTargetProperties = new HashMap<>(getBaseTargetProperties());
		this.defaultTargetProperties.putAll(defaultTargetProperties);
	}
}
