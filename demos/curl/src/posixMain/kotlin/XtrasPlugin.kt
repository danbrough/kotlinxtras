import org.gradle.api.Plugin
import org.gradle.api.Project

class XtrasPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    project.log("applying XtrasPlugin to ${project.projectDir.absolutePath}")


    project.afterEvaluate {
      project.log("afterEvaluate in XtrasPlugin at ${project.projectDir.absolutePath}")
    }
  }
}
