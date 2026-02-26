/***************************************
 * Bad Scala Spark Code Example - Multiple Violations
 * This code demonstrates common anti-patterns and violations
 */

import org.apache.spark.sql.{SparkSession, DataFrame}
import org.apache.spark.sql.functions._

object BadScalaExample {

  // VIOLATION: Not using case classes for type safety
  def processDataWithoutSchema(): DataFrame = {
    val spark = SparkSession.builder().getOrCreate()

    // BAD: Using generic Row instead of typed Dataset
    val df = spark.read.json("s3://bucket/data.json")
    df.select("name", "age", "salary")  // No compile-time type checking
  }

  // VIOLATION: Mutable variables
  def calculateWithMutability(): Long = {
    val spark = SparkSession.builder().getOrCreate()
    val df = spark.read.table("transactions")

    // BAD: Using var instead of val
    var total = 0L
    var count = 0

    df.collect().foreach { row =>
      total += row.getAs[Long]("amount")
      count += 1
    }

    total / count
  }

  // VIOLATION: Not handling Option types properly
  def unsafeOptionHandling(): DataFrame = {
    val spark = SparkSession.builder().getOrCreate()
    val df = spark.read.table("users")

    // BAD: Using .get without checking if Option is defined
    val config = Map("timeout" -> "30")
    val timeout = config.get("timeout").get  // Will throw NoSuchElementException if missing

    df.filter(col("last_login") > timeout)
  }

  // VIOLATION: Inefficient filtering
  def inefficientFiltering(): DataFrame = {
    val spark = SparkSession.builder().getOrCreate()
    val df = spark.read.table("events")

    // BAD: Multiple separate filters instead of combined
    val result = df
      .filter(col("status") === "active")
      .filter(col("country") === "US")
      .filter(col("age") > 18)
      .filter(col("verified") === true)

    // Should combine: filter(col("status") === "active" && col("country") === "US" && ...)
    result
  }

  // VIOLATION: Resource leak - not closing connections
  def resourceLeak(): Unit = {
    val spark = SparkSession.builder().getOrCreate()

    // BAD: Opening connection but not closing it
    val connection = java.sql.DriverManager.getConnection(
      "jdbc:mysql://localhost/db",
      "user",
      "password"
    )

    val statement = connection.createStatement()
    val resultSet = statement.executeQuery("SELECT * FROM users")

    // Missing: connection.close(), statement.close(), resultSet.close()
  }

  // VIOLATION: Unnecessary shuffles
  def unnecessaryShuffle(): DataFrame = {
    val spark = SparkSession.builder().getOrCreate()
    val df = spark.read.table("sales")

    // BAD: Repartition followed immediately by another repartition
    val result = df
      .repartition(100)
      .repartition(50)  // Unnecessary second shuffle
      .select("product", "amount")

    result
  }

  // VIOLATION: Not using appropriate data types
  def wrongDataTypes(): DataFrame = {
    val spark = SparkSession.builder().getOrCreate()

    // BAD: Storing timestamp as string
    val df = spark.read
      .option("header", "true")
      .csv("s3://bucket/events.csv")

    // Should use: .withColumn("timestamp", to_timestamp(col("timestamp"), "yyyy-MM-dd HH:mm:ss"))
    df.select("event_id", "timestamp", "user_id")
  }

  // VIOLATION: Blocking operations in transformation
  def blockingInTransformation(): DataFrame = {
    val spark = SparkSession.builder().getOrCreate()
    val df = spark.read.table("requests")

    // BAD: HTTP call inside map - blocking and slow
    import spark.implicits._
    val result = df.map { row =>
      val url = row.getAs[String]("api_url")
      // BAD: Synchronous HTTP call
      val response = scala.io.Source.fromURL(url).mkString
      (row.getAs[String]("id"), response)
    }.toDF("id", "response")

    result
  }

  // VIOLATION: SQL injection vulnerability
  def sqlInjectionVulnerable(userInput: String): DataFrame = {
    val spark = SparkSession.builder().getOrCreate()

    // BAD: String interpolation in SQL - SQL injection risk
    val query = s"SELECT * FROM users WHERE username = '$userInput'"
    spark.sql(query)

    // Should use parameterized queries
  }

  // VIOLATION: Memory leak - accumulating data
  def memoryLeak(): Unit = {
    val spark = SparkSession.builder().getOrCreate()

    // BAD: Accumulating DataFrames in memory without cleanup
    var dataFrames = List.empty[DataFrame]

    for (i <- 1 to 1000) {
      val df = spark.read.parquet(s"s3://bucket/data_$i")
      dataFrames = dataFrames :+ df  // Memory keeps growing
    }

    // Should process and release
  }

  // VIOLATION: Inefficient string concatenation
  def inefficientStringConcat(): DataFrame = {
    val spark = SparkSession.builder().getOrCreate()
    val df = spark.read.table("users")

    // BAD: Multiple concat operations
    val result = df
      .withColumn("full_name", concat(col("first_name"), lit(" ")))
      .withColumn("full_name", concat(col("full_name"), col("last_name")))
      .withColumn("full_address", concat(col("street"), lit(", ")))
      .withColumn("full_address", concat(col("full_address"), col("city")))

    // Should do in single concat call
    result
  }

  // VIOLATION: Not using partitioning for writes
  def unpartitionedWrite(): Unit = {
    val spark = SparkSession.builder().getOrCreate()
    val df = spark.read.table("events")

    // BAD: Writing large dataset without partitioning
    df.write
      .mode("overwrite")
      .parquet("s3://bucket/events_archive")

    // Should use: .partitionBy("date", "region")
  }

  // VIOLATION: Catching generic Exception
  def poorExceptionHandling(): DataFrame = {
    val spark = SparkSession.builder().getOrCreate()

    try {
      spark.read.table("non_existent_table")
    } catch {
      // BAD: Catching all exceptions and ignoring
      case e: Exception =>
        println("Error occurred")
        spark.emptyDataFrame
    }
  }

  // VIOLATION: Not using window functions efficiently
  def inefficientRanking(): DataFrame = {
    val spark = SparkSession.builder().getOrCreate()
    val df = spark.read.table("sales")

    // BAD: Using UDF for ranking instead of window function
    import spark.implicits._
    val grouped = df.groupBy("category").agg(collect_list("product").as("products"))

    // Should use: window().partitionBy("category").orderBy(desc("sales")).rank()
    grouped
  }

  // VIOLATION: Hardcoded paths and configurations
  def hardcodedConfig(): DataFrame = {
    val spark = SparkSession.builder().getOrCreate()

    // BAD: Hardcoded S3 paths and configs
    val df = spark.read
      .option("header", "true")
      .option("delimiter", ",")
      .csv("s3://my-prod-bucket/sensitive-data/2024/01/15/data.csv")

    // Should use configuration management
    df
  }

  // VIOLATION: Not persisting expensive computations
  def expensiveRecomputation(): (Long, Long, Long) = {
    val spark = SparkSession.builder().getOrCreate()
    val df = spark.read.table("transactions")

    // BAD: Complex transformation recomputed 3 times
    val expensive = df
      .join(spark.read.table("customers"), "customer_id")
      .join(spark.read.table("products"), "product_id")
      .filter(col("amount") > 1000)

    // Each action recomputes the entire lineage
    val count1 = expensive.count()
    val sum1 = expensive.agg(sum("amount")).head().getLong(0)
    val avg1 = expensive.agg(avg("amount")).head().getDouble(0)

    // Should use: expensive.persist()
    (count1, sum1, avg1.toLong)
  }

  // VIOLATION: Using null checks instead of Option
  def nullChecksInsteadOfOption(): String = {
    val name: String = null

    // BAD: Java-style null checking
    if (name != null) {
      name.toUpperCase
    } else {
      "UNKNOWN"
    }

    // Should use: Option(name).map(_.toUpperCase).getOrElse("UNKNOWN")
  }

  // VIOLATION: Monolithic function - too many responsibilities
  def godFunction(): Unit = {
    val spark = SparkSession.builder().getOrCreate()

    // BAD: 500-line function doing everything
    val df = spark.read.table("raw_data")
    val cleaned = df.na.drop()
    val filtered = cleaned.filter(col("status") === "active")
    val enriched = filtered.join(spark.read.table("lookup"), "id")
    val aggregated = enriched.groupBy("category").agg(sum("amount"))
    val formatted = aggregated.withColumn("formatted_amount", format_number(col("amount"), 2))
    formatted.write.mode("overwrite").table("output")

    // Notifications, logging, error handling all in one place
    // Should be broken into smaller, focused functions
  }

  // VIOLATION: Magic numbers
  def magicNumbers(): DataFrame = {
    val spark = SparkSession.builder().getOrCreate()
    val df = spark.read.table("users")

    // BAD: Magic numbers without explanation
    val result = df
      .filter(col("age") > 18)
      .filter(col("score") >= 750)
      .filter(col("balance") < 10000)
      .repartition(200)

    // Should define: val MIN_ADULT_AGE = 18, val MIN_CREDIT_SCORE = 750, etc.
    result
  }
}
