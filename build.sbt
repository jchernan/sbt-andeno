name := """sbt-andeno"""
organization := "com.andeno"

homepage := Some(url("https://github.com/jchernan/sbt-andeno"))

licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0"))

version := "0.1.0"

sbtPlugin := true

val scalastyleDir = Def.setting(baseDirectory.value / "project")

scalastyleConfig := scalastyleDir.value / "scalastyle-config.xml"
scalastyleFailOnError := true

publishMavenStyle := true
pomIncludeRepository := { _ => false }

bintrayPackageLabels := Seq("sbt", "plugin")
bintrayVcsUrl := Some("""git@github.com:jchernan/sbt-andeno.git""")

initialCommands in console := """import com.andeno.sbt._"""
