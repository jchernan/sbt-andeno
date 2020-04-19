/*
 * Copyright (c) 2016-2020 Andeno Co. All rights reserved.
 */

package com.andeno.sbt

import java.io.{BufferedWriter, FileWriter}

import sbt._
import Keys._
import com.typesafe.config.ConfigFactory

import scala.io.Source

/**
  *
  */
object SbtPlugin extends AutoPlugin {

  object autoImport {
    // scalastyle:off
    val productionPort = settingKey[Option[Int]]("HTTP server port to use in production.")
    val dbUrl = settingKey[Option[String]]("URL of the app database.")
    val dbUser = settingKey[Option[String]]("Username for the app database.")
    val dbPassword = settingKey[Option[String]]("Password for the app database.")
    val tablesPackage = settingKey[String]("Destination package for the Slick Table classes.")
    val generateSlickTables = taskKey[File]("Generates Slick Table classes.")
    val generateProductionScripts = taskKey[Seq[File]]("Generates scripts to use in production.")
    // scalastyle:on
  }

  import autoImport._
  override lazy val projectSettings = Seq(
    productionPort := None,
    dbUrl := url,
    dbUser := username,
    dbPassword := password,
    tablesPackage := "daos",
    generateSlickTables := generateTablesTask.value,
    generateProductionScripts := generateScriptsTask.value
  )

  private lazy val configFile = new File("conf/application.conf")
  private lazy val conf = ConfigFactory.parseFile(configFile).resolve()
  private def url = getConfigString("db.default.url")
  private def username = getConfigString("db.default.username")
  private def password = getConfigString("db.default.password")
  private def driver = getConfigString("db.default.driver")
  private def slickProfile = getConfigString("slick.dbs.default.profile")

  private def getConfigString(key: String) = {
    if (conf.hasPath(key)) Some(conf.getString(key)) else None
  }

  lazy val generateTablesTask = Def.task {
    val outputDir = (sourceManaged.value / "slick").getPath
    // value in configuration has a $ character at the end
    val profile = slickProfile.map(p => p.substring(0, p.length - 1))
    val pkg = tablesPackage.value
    (runner in Compile).value.run(
      "slick.codegen.SourceCodeGenerator",
      (dependencyClasspath in Compile).value.files,
      Array(
        profile.get,
        driver.get,
        url.get,
        outputDir,
        pkg,
        username.get,
        password.get,
        "true", // ignoreInvalidDefaults
        "com.andeno.play.SlickCodeGenerator",
        "false" // outputToMultipleFiles
      ),
      streams.value.log
    ).failed foreach (sys error _.getMessage)
    val originalOut = file(s"$outputDir/$pkg/Tables.scala")
    val modifiedOut = file(s"app/$pkg/Tables.scala")
    val writer = new BufferedWriter(new FileWriter(modifiedOut))
    writer.write("// scalastyle:off\n")
    Source.fromFile(originalOut).getLines.foreach(l => writer.write(s"$l\n"))
    writer.write("// scalastyle:on\n")
    writer.close()
    modifiedOut
  }

  private lazy val scripts = Seq(
    "appspec.yml",
    "scripts/clean.sh",
    "scripts/install.sh",
    "scripts/run.sh",
    "scripts/start.sh",
    "scripts/stop.sh"
  )

  lazy val generateScriptsTask = Def.task {
    file("dist/scripts").mkdirs()
    val sources = scripts.map(Source.fromResource(_, getClass.getClassLoader))
    scripts.zip(sources).map { case (n, s) =>
      val out = file(s"dist/$n")
      val writer = new BufferedWriter(new FileWriter(out))
      s.getLines.foreach(l => {
        val port = productionPort.value.map(_.toString).getOrElse("")
        val m = l.replaceAll("\\$APP", name.value).replaceAll("\\$PORT", port)
        writer.write(s"$m\n")
      })
      writer.close()
      out
    }
  }
}
