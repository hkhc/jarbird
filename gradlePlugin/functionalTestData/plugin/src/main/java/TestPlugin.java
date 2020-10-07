import org.gradle.api.Plugin
import org.gradle.api.Project

public class TestPlugin extends Plugin<Project> {
    @Override
    public void apply(project: Project) {
        System.out.println("Hello world "+project.getName());
    }
}
