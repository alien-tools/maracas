package com.github.maracas.build;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;

/**
 * Handles the build process of a Maven project.
 */
public class MavenBuildHandler implements BuildHandler {
    /**
     * Class logger
     */
    private static final Logger logger = LogManager.getLogger(MavenBuildHandler.class);

    /**
     * Maven build configuration
     */
    private MavenBuildConfig config;

    /**
     * Creates a MavenCompilationHandler instance using the default
     * {@link MavenBuildConfig}.
     *
     * @param srcDirPath path to the source code directory
     */
    public MavenBuildHandler(Path srcDirPath) {
        this(srcDirPath, null);
    }

    /**
     * Creates a MavenCompilationHandler instance using a user-defined
     * {@link MavenBuildConfig}.
     *
     * @param srcDirPath path to the source code directory
     * @param config     user-defined Maven build configuration
     */
    public MavenBuildHandler(Path srcDirPath, MavenBuildConfig config) {
        this.config = config != null ? config : new MavenBuildConfig(srcDirPath.toString());
    }

    @Override
    public void build() {
        File srcDir = new File(config.srcDir());
        if (!srcDir.exists())
           throw new BuildException("The source directory cannot be found");

        File pom = srcDir.toPath().resolve(Paths.get(config.pom())).toFile();
        if (!pom.exists())
            throw new BuildException("The POM file of the projcet cannot be found");

        Properties properties = new Properties();
        config.properties().forEach(p -> properties.put(p, "true"));
        List<String> goals = config.goals();

        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile(pom);
        request.setGoals(goals);
        request.setProperties(properties);
        request.setBatchMode(true);

        logger.info("Building {} with pom={} goals={} properties={}",
            srcDir, pom, goals, properties);
        executeRequest(request);
    }

    /**
     * Executes an invocation request to the Maven invoker. Throws an exception
     * if there is an execution exception or if the exit code of the execution
     * is different from zero.
     *
     * @param request Maven {@link InvocationRequest}
     */
    private void executeRequest(InvocationRequest request) {
        try {
            Invoker invoker = new DefaultInvoker();
            InvocationResult result = invoker.execute(request);

            if (result.getExecutionException() != null)
                throw new BuildException(String.format("%s failed: %s",
                    request.getGoals(), result.getExecutionException()));

            if (result.getExitCode() != 0)
                throw new BuildException(String.format("%s failed: %s",
                    request.getGoals(), result.getExitCode()));
        } catch (MavenInvocationException e) {
            throw new BuildException(e);
        }
    }

    @Override
    public List<CompilerMessage> gatherCompilerMessages() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Updates the POM file of Maven project.
     *
     * @return true if the POM file has been successfully updated, false
     * otherwise.
     */
    public boolean updatePOM() {
        return false;
    }

}
