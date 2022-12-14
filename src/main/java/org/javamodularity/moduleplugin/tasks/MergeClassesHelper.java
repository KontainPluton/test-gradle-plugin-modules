package org.javamodularity.moduleplugin.tasks;

import org.gradle.api.Project;
import org.gradle.api.file.DuplicatesStrategy;
import org.gradle.api.file.FileCollection;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.Sync;
import org.gradle.api.tasks.compile.AbstractCompile;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.jvm.tasks.Jar;
import org.javamodularity.moduleplugin.JavaProjectHelper;
import org.javamodularity.moduleplugin.extensions.CompileModuleOptions;
import org.javamodularity.moduleplugin.internal.StreamHelper;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public class MergeClassesHelper {
    private static final Logger LOGGER = Logging.getLogger(MergeClassesHelper.class);

    public static final String MERGE_CLASSES_TASK_NAME = "mergeClasses";
    public static final List<String> PRE_JAVA_COMPILE_TASK_NAMES = List.of("compileKotlin", JavaProjectHelper.COMPILE_TEST_FIXTURES_KOTLIN_TASK_NAME);
    public static final List<String> POST_JAVA_COMPILE_TASK_NAMES = List.of("compileGroovy");

    private final Project project;

    public MergeClassesHelper(Project project) {
        this.project = project;
    }

    public Project project() {
        return project;
    }

    public Stream<AbstractCompile> otherCompileTaskStream() {
        return otherCompileTaskNameStream()
                .map(name -> helper().findTask(name, AbstractCompile.class))
                .flatMap(Optional::stream);
    }

    private Stream<String> otherCompileTaskNameStream() {
        return StreamHelper.concat(
                PRE_JAVA_COMPILE_TASK_NAMES.stream(),
                POST_JAVA_COMPILE_TASK_NAMES.stream(),
                Stream.of(CompileModuleOptions.COMPILE_MODULE_INFO_TASK_NAME)
        );
    }

    public JavaCompile javaCompileTask() {
        return helper().task(JavaPlugin.COMPILE_JAVA_TASK_NAME, JavaCompile.class);
    }

    public Stream<AbstractCompile> allCompileTaskStream() {
        return Stream.concat(Stream.of(javaCompileTask()), otherCompileTaskStream());
    }

    public boolean isMergeRequired() {
        return otherCompileTaskStream().anyMatch(task -> !task.getSource().isEmpty());
    }

    public Sync createMergeClassesTask() {
        Sync sync = project.getTasks().create(MERGE_CLASSES_TASK_NAME, Sync.class);
        sync.setDuplicatesStrategy(DuplicatesStrategy.INCLUDE);
        return sync;
    }

    public FileCollection getMergeAdjustedClasspath(FileCollection classpath) {
        File testFixturesJar = helper().findTask("testFixturesJar", Jar.class)
                .map(task -> task.getArchiveFile().get().getAsFile())
                .orElse(null);
        if(testFixturesJar != null) {
            classpath = classpath.minus(project.files(testFixturesJar));
        }

        boolean mergeRequired = isMergeRequired();
        if (!mergeRequired) {
            return classpath;
        }

        Set<File> files = new HashSet<>(classpath.getFiles());
        allCompileTaskStream().map(AbstractCompile::getDestinationDir).forEach(files::remove);
        files.add(helper().getMergedDir());
        return project.files(files.toArray());
    }

    private JavaProjectHelper helper() {
        return new JavaProjectHelper(project);
    }
}
