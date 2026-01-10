package io.onedev.server.ai.tools;

import java.util.ArrayList;

import org.apache.shiro.subject.Subject;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Joiner;

import dev.langchain4j.agent.tool.ToolSpecification;
import io.onedev.server.ai.BuildSpecSchema;
import io.onedev.server.ai.TaskTool;
import io.onedev.server.ai.ToolExecutionResult;
import io.onedev.server.buildspec.job.JobVariable;

public class GetBuildSpecEditInstructions implements TaskTool {

    private final boolean withSaveInstruction;

    public GetBuildSpecEditInstructions(boolean withSaveInstruction) {
        this.withSaveInstruction = withSaveInstruction;
    }

    @Override
    public ToolSpecification getSpecification() {
        return ToolSpecification.builder()
            .name("getBuildSpecEditInstructions")
            .description("Get instructions on how to edit various parts of CI/CD spec, including jobs, services, step templates, properties, and imports")
            .build();
    }
    
    @Override
    public ToolExecutionResult execute(Subject subject, JsonNode arguments) {
        var variables = new ArrayList<String>();
        for (JobVariable variable: JobVariable.values()) {
            variables.add("- @%s@".formatted(variable.name().toLowerCase()));
        }
        variables.add("- @secret:<job secret name>@ (get value of specified job secret)");
        variables.add("- @file:<workspace file path>@ (get content of specified workspace file generated in previous steps)");
        
        var instructions = """
            OneDev CI/CD spec is a yaml file conforming to below schema:

            <!SCHEMA BEGIN!>
            %s
            <!SCHEMA END!>							

            Available variables that can be used in CI/CD spec:
            %s
            
            When editing CI/CD spec, remember that:

            1. Files in job workspace are shared between different steps. So you can generate workspace files in one step, and use them in another step.
            2. If command step is used, turn on the "run in container" if possible, unless requested by user explicitly
            3. Different steps run in isolated environments (only job workspace is shared). So it will not work installing dependencies in one step, and run commands relying on them in another step. You should put them in a single step unless requested by user explicitly
            4. If cache step is used:
                4.1 It should be placed before the step building or testing the project
                4.2 If the project has lock files (package.json, pom.xml, etc.):
                    4.2.1 A generate checksum step should be placed before the cache step, to generate checksum of all relevant lock files and store it in a file named checksum.txt						
                    4.2.2 The key property should be configured as <keyname>-@file:checksum.txt@
                    4.2.3 The load keys property should be configured as <keyname>
                    4.2.4 The upload strategy property should be configured as UPLOAD_IF_NOT_HIT
            5. If user wants to pass files between different jobs, one job should publish files via the publish artifact step, and another jobs can then download them into job workspace via job dependency
            6. Call tools such as getRootFilesAndFolders, getFilesAndSubfolders and getTextContent to get project structure, and figure out what docker image and commands to use to build or test the project if requested by user"""
            .formatted(BuildSpecSchema.get(), Joiner.on("\n").join(variables));

        if (withSaveInstruction)
            instructions += "\n7. After editing the CI/CD spec, call saveBuildSpec tool to save the result. If saving fails, fix errors according to error messages and schema above and save again";
        
        return new ToolExecutionResult(instructions, true);        
    }

}