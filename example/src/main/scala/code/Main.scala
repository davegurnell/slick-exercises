package code

import slick.jdbc.H2Profile.api._
import slick.jdbc.H2Profile

import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

object Main {
  val store = new AlbumStore(H2Profile)
  val database = Database.forConfig("my.awesome.album.example")

  val program: DBIO[List[Album]] =
    store.initialise
      .andThen(store.topFiveAlbums)

  def main(args: Array[String]): Unit = {
    val albums: List[Album] =
      Await.result(database.run(program), 5.seconds)

    albums.foreach(println)
  }
}
