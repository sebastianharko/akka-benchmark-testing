package testing

import java.util.UUID

import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._

import scala.concurrent.duration._

class BasicItSimulation extends Simulation {

  def randomId : String = UUID.randomUUID().toString

  val httpConf = http
    .baseURL("http://localhost:8080/")

  val scn: ScenarioBuilder = scenario("Only Scenario").exec(
    http("ping request")
      .post("ping")
      .body(StringBody(session => s"""{"id": "${randomId}"}"""))
      .header("Content-Type", "application/json")
    )

  setUp(scn.inject(constantUsersPerSec(500) during(10 minutes))).protocols(httpConf)

}

