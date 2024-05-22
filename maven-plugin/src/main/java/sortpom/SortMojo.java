package sortpom;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import sortpom.exception.ExceptionConverter;
import sortpom.logger.MavenLogger;
import sortpom.parameter.PluginParameters;

import java.util.Optional;

/**
 * Sorts the pom.xml for a Maven project.
 *
 * @author Bjorn Ekryd
 */
@Mojo(name = "sort", threadSafe = true, defaultPhase = LifecyclePhase.VALIDATE)
@SuppressWarnings({"UnusedDeclaration"})
public class SortMojo extends AbstractParentMojo {

    public void setup() throws MojoFailureException {
        new ExceptionConverter(() -> {
            String dependencyPriorityGroups = getDependencyPriorityGroupsRendered();
            String pluginPriorityGroups = getPluginPriorityGroupsRendered();
            PluginParameters pluginParameters = PluginParameters.builder()
                    .setPomFile(pomFile)
                    .setFileOutput(createBackupFile, backupFileExtension, null, keepTimestamp)
                    .setEncoding(encoding)
                    .setFormatting(lineSeparator, expandEmptyElements, spaceBeforeCloseEmptyElement, keepBlankLines)
                    .setIndent(nrOfIndentSpace, indentBlankLines, indentSchemaLocation)
                    .setSortOrder(sortOrderFile, predefinedSortOrder)
                    .setSortEntities(sortDependencies, sortDependencyExclusions, sortPlugins, sortProperties, sortModules, sortExecutions, dependencyPriorityGroups, pluginPriorityGroups)
                    .setIgnoreLineSeparators(ignoreLineSeparators)
                    .build();

            sortPomImpl.setup(new MavenLogger(getLog()), pluginParameters);
        }).executeAndConvertException();
    }

    protected void sortPom() throws MojoFailureException {
        new ExceptionConverter(sortPomImpl::sortPom).executeAndConvertException();
    }

}
