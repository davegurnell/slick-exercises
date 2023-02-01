package code

import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext

class AlbumStore(val profile: JdbcProfile)(implicit ec: ExecutionContext) {
  import profile.api._

  class AlbumTable(tag: Tag) extends Table[Album](tag, "albums") {
    def artist = column[String]("artist")
    def title = column[String]("title")
    def year = column[Int]("year")
    def stars = column[Int]("stars")
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def * = (artist, title, year, stars, id).mapTo[Album]
  }

  lazy val AlbumTable = TableQuery[AlbumTable]

  private val recreateTables: DBIO[Unit] =
    AlbumTable.schema.dropIfExists
      .andThen(AlbumTable.schema.create)

  private val insertTestData: DBIO[Option[Int]] =
    AlbumTable ++= Seq(
      Album("Daft Punk", "Alive 2007", year = 2007, stars = 4),
      Album("Justin Bieber", "Believe", year = 2013, stars = 1),
      Album("Keyboard Cat", "Keyboard Cat's Greatest Hits", year = 2009, stars = 5),
      Album("Manowar", "The Triumph of Steel", year = 1992, stars = 2),
      Album("Pink Floyd", "Dark Side of the Moon", year = 1978, stars = 5),
      Album("Rick Astley", "Whenever You Need Somebody", year = 1987, stars = 3),
      Album("Spice Girls", "Spice", year = 1996, stars = 4),
    )

  val initialise: DBIO[Unit] =
    recreateTables.andThen(insertTestData).map(_ => ())

  val topAlbumsQuery: Query[AlbumTable, Album, List] =
    AlbumTable
      .sortBy(a => (a.stars.desc, a.artist.asc))
      .to[List]

  def albumsByYearQuery(from: Int, to: Int): Query[AlbumTable, Album, List] =
    AlbumTable
      .filter(a => a.year >= from && a.year <= to)
      .sortBy(a => (a.year.asc, a.artist.asc))
      .to[List]

  val topFiveAlbums: DBIO[List[Album]] =
    topAlbumsQuery.take(5).result
}
