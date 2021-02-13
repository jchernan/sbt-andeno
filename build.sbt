name := """sbt-andeno"""
organization := "com.andeno"

licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0"))

version := "0.2.0"

sbtPlugin := true

val scalastyleDir = Def.setting(baseDirectory.value / "project")

scalastyleConfig := scalastyleDir.value / "scalastyle-config.xml"
scalastyleFailOnError := true

githubOwner := "jchernan"
githubRepository := "sbt-andeno"

initialCommands in console := """import com.andeno.sbt._"""
