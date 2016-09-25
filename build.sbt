name := "Akka HTTP with Akka Streams and Akka Actors Integration"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies ++= {
  val akka = "com.typesafe.akka"
  val akkaV = "2.4.10"
  val scalaTestV = "3.0.0"
  Seq(
    akka                        %% "akka-actor"                           % akkaV,
    akka                        %% "akka-testkit"                         % akkaV % "test",
    akka                        %% "akka-slf4j"                           % akkaV,
    akka                        %% "akka-http-core"                       % akkaV,
    akka                        %% "akka-http-experimental"               % akkaV,
    akka                        %% "akka-http-spray-json-experimental"    % akkaV,
    akka                        %% "akka-http-testkit"                    % akkaV,
    "de.heikoseeberger"         %% "akka-sse"                             % "1.10.0",
    "ch.megard"                 %% "akka-http-cors"                       % "0.1.6",
    "ch.qos.logback"            % "logback-classic"                       % "1.1.7",
    "org.codehaus.groovy"       % "groovy"                                % "2.4.7"
  )
}