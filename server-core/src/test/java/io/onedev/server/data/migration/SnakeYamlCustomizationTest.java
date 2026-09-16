package io.onedev.server.data.migration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import io.onedev.commons.loader.AppLoader;
import io.onedev.commons.loader.AppLoaderMocker;
import io.onedev.commons.loader.ImplementationRegistry;
import io.onedev.server.annotation.Editable;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.buildspec.step.CommandStep;
import io.onedev.server.buildspec.step.commandinterpreter.PosixInterpreter;

public class SnakeYamlCustomizationTest extends AppLoaderMocker {

    @Override
    protected void setup() {
        when(AppLoader.getInstance(ImplementationRegistry.class)).thenReturn(new ImplementationRegistry() {
            @Override
            public <T> Collection<Class<? extends T>> getImplementations(Class<T> type) {
                var implementations = new ArrayList<Class<? extends T>>();
                for (var candidate : List.of(Command.class, CommandStep.class, PosixInterpreter.class)) {
                    if (type.isAssignableFrom(candidate))
                        implementations.add(candidate.asSubclass(type));
                }
                return implementations;
            }
        });
    }

    @Override
    protected void teardown() {
    }

    @Test
    public void clearsOnlyWritableEditableReferenceDefaultsWhenLoading() {
        var bean = VersionedYamlDoc.fromYaml("version: 0\n").toBean(Defaults.class);
        assertNull(bean.getName());
        assertNull(bean.getCommand());
        assertTrue(bean.getNames().isEmpty());
        assertTrue(bean.getVariables().isEmpty());
        assertEquals(7, bean.getRetries());
        assertTrue(bean.isEnabled());
        assertEquals("internal default", bean.getInternal());
        assertEquals("read only", bean.getReadOnly());
        // Cleared collections must still be mutable for the build-spec editor.
        bean.getNames().add("new");
        bean.getVariables().put("key", "value");
    }

    @Test
    public void loadsExplicitValuesAfterClearingEditableDefaults() {
        var bean = VersionedYamlDoc.fromYaml("""
                version: 0
                name: configured
                names: [one, two]
                variables: {key: value}
                retries: 3
                enabled: false
                command: {script: echo hello}
                """).toBean(Defaults.class);
        assertEquals("configured", bean.getName());
        assertEquals(List.of("one", "two"), bean.getNames());
        assertEquals(Map.of("key", "value"), bean.getVariables());
        assertEquals(3, bean.getRetries());
        assertFalse(bean.isEnabled());
        assertEquals("echo hello", bean.getCommand().getScript());
    }

    @Test
    public void retainsDefaultsOfNonEditableBeans() {
        var bean = VersionedYamlDoc.fromYaml("version: 0\n").toBean(PlainDefaults.class);
        assertEquals("plain default", bean.getName());
    }

    @Test
    public void representsPolymorphicPropertiesListsAndArraysWithTypeEntries() {
        var bean = new Actions();
        bean.setAction(new Command("single"));
        bean.setConcrete(new Command("concrete"));
        bean.setActions(Arrays.asList(new Command("list"), null));
        bean.setArray(new Action[] {new Command("array"), null});
        bean.setLabels(Map.of("type", "user data"));

        var document = VersionedYamlDoc.fromBean(bean);
        assertEquals(Tag.MAP, document.getTag());
        // Safe loading must succeed without Java class tags or application-specific YAML tags.
        Map<String, Object> data = new Yaml().load(document.toYaml());
        assertEquals(Map.of("type", "Command", "script", "single"), data.get("action"));
        assertEquals(Map.of("script", "concrete"), data.get("concrete"));
        assertEquals(Arrays.asList(Map.of("type", "Command", "script", "list"), null), data.get("actions"));
        assertEquals(Arrays.asList(Map.of("type", "Command", "script", "array"), null), data.get("array"));
        assertEquals(Map.of("type", "user data"), data.get("labels"));

        var restored = VersionedYamlDoc.fromYaml(document.toYaml()).toBean(Actions.class);
        assertEquals("single", assertInstanceOf(Command.class, restored.getAction()).getScript());
        assertEquals("concrete", restored.getConcrete().getScript());
        assertEquals("list", assertInstanceOf(Command.class, restored.getActions().get(0)).getScript());
        assertNull(restored.getActions().get(1));
        assertEquals("array", assertInstanceOf(Command.class, restored.getArray()[0]).getScript());
        assertNull(restored.getArray()[1]);
    }

    @Test
    public void representsEachPolymorphicSetMemberWithItsOwnTypeEntry() {
        var bean = new ActionSet();
        bean.setActions(new LinkedHashSet<>(List.of(new Command("first"), new Command("second"))));
        var root = (MappingNode) new Representer(new DumperOptions()).represent(bean);
        var set = (MappingNode) value(root, "actions");
        assertEquals(Tag.SET, set.getTag());
        assertEquals(2, set.getValue().size());
        for (var tuple : set.getValue()) {
            var member = assertInstanceOf(MappingNode.class, tuple.getKeyNode());
            assertEquals(Tag.MAP, member.getTag());
            assertEquals("Command", ((ScalarNode) value(member, "type")).getValue());
        }
    }

    @Test
    public void emitsMutatedScalarValuesUsedByYamlMigrations() {
        var document = VersionedYamlDoc.fromYaml("version: 0\n");
        var name = new ScalarNode(Tag.STR, "before");
        assertEquals(DumperOptions.ScalarStyle.PLAIN, name.getScalarStyle());
        document.getValue().add(new NodeTuple(new ScalarNode(Tag.STR, "name"), name));
        name.setValue("after: migration");
        var restored = VersionedYamlDoc.fromYaml(document.toYaml()).toBean(Defaults.class);
        assertEquals("after: migration", restored.getName());
    }

    @Test
    public void roundTripsAProductionBuildSpecWithNestedPolymorphicSteps() {
        var yaml = """
                version: %s
                jobs:
                - name: test
                  steps:
                  - type: CommandStep
                    name: execute
                    runInContainer: false
                    interpreter:
                      type: PosixInterpreter
                      shell: bash
                      commands: echo hello
                    condition: SUCCESSFUL
                    optional: false
                """.formatted(MigrationHelper.getVersion(BuildSpec.class));
        BuildSpec spec = VersionedYamlDoc.fromYaml(yaml).toBean(BuildSpec.class);
        var emitted = VersionedYamlDoc.fromBean(spec).toYaml();
        BuildSpec restored = VersionedYamlDoc.fromYaml(emitted).toBean(BuildSpec.class);
        assertEquals(1, restored.getJobs().size());
        assertEquals("test", restored.getJobs().get(0).getName());
        assertEquals(1, restored.getJobs().get(0).getSteps().size());
        var step = assertInstanceOf(CommandStep.class, restored.getJobs().get(0).getSteps().get(0));
        assertEquals("execute", step.getName());
        var interpreter = assertInstanceOf(PosixInterpreter.class, step.getInterpreter());
        assertEquals("bash", interpreter.getShell());
        assertEquals("echo hello", interpreter.getCommands());
        assertEquals(new Yaml().<Object>load(emitted),
                new Yaml().<Object>load(VersionedYamlDoc.fromBean(restored).toYaml()));
    }

    private static Node value(MappingNode node, String name) {
        return node.getValue().stream()
                .filter(tuple -> ((ScalarNode) tuple.getKeyNode()).getValue().equals(name))
                .findFirst().orElseThrow().getValueNode();
    }

    @Editable
    public static class Defaults {
        private String name = "default";
        private Command command = new Command("default");
        private List<String> names = new ArrayList<>(List.of("default"));
        private Map<String, String> variables = new LinkedHashMap<>(Map.of("default", "value"));
        private int retries = 7;
        private boolean enabled = true;
        private String internal = "internal default";

        @Editable public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        @Editable public Command getCommand() { return command; }
        public void setCommand(Command command) { this.command = command; }
        @Editable public List<String> getNames() { return names; }
        public void setNames(List<String> names) { this.names = names; }
        @Editable public Map<String, String> getVariables() { return variables; }
        public void setVariables(Map<String, String> variables) { this.variables = variables; }
        @Editable public int getRetries() { return retries; }
        public void setRetries(int retries) { this.retries = retries; }
        @Editable public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public String getInternal() { return internal; }
        public void setInternal(String internal) { this.internal = internal; }
        @Editable public String getReadOnly() { return "read only"; }
    }

    public static class PlainDefaults {
        private String name = "plain default";
        @Editable public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    @Editable
    public abstract static class Action {
    }

    @Editable
    public static class Command extends Action {
        private String script;
        public Command() { }
        public Command(String script) { this.script = script; }
        @Editable public String getScript() { return script; }
        public void setScript(String script) { this.script = script; }
    }

    @Editable
    public static class Actions {
        private Action action;
        private Command concrete;
        private List<Action> actions;
        private Action[] array;
        private Map<String, String> labels;

        @Editable public Action getAction() { return action; }
        public void setAction(Action action) { this.action = action; }
        @Editable public Command getConcrete() { return concrete; }
        public void setConcrete(Command concrete) { this.concrete = concrete; }
        @Editable public List<Action> getActions() { return actions; }
        public void setActions(List<Action> actions) { this.actions = actions; }
        @Editable public Action[] getArray() { return array; }
        public void setArray(Action[] array) { this.array = array; }
        @Editable public Map<String, String> getLabels() { return labels; }
        public void setLabels(Map<String, String> labels) { this.labels = labels; }
    }

    public static class ActionSet {
        private Set<Action> actions;
        public Set<Action> getActions() { return actions; }
        public void setActions(Set<Action> actions) { this.actions = actions; }
    }
}
