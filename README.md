
# Metrics

A lightweight custom metrics library that exposes Apache Spark's internal metric registry.

# Motivation

This library is a lightweight way to inject custom metrics into your Apache Spark application leveraging Spark's 
internal metric registry. 

Do use this library if you want to send metrics to remote system (e.g. graphite)

Don't use this library if you want to see all of the metrics aggregated on the driver.

# Usage

```scala
import org.apache.spark.metrics.mz.CustomMetrics

val metrics = new CustomMetrics("my-metrics")
val meter = metrics.meter("met")
meter.mark(10)
```

you will see metrics populated under the key `APP_ID.EXECUTOR_ID.my-metrics.met`

# Development

Clone this repository and run `mvn clean test`

To build for a custom version of Spark/Scala, run 
`mvn clean package \
-Dscala.major.version=<SCALA_MAJOR> \
-Dscala.minor.version=<SCALA_MINOR> \
-Dspark.version=<SPARK_VERSION>`

e.g. 
```bash
mvn clean compile \
-Dscala.major.version=2.10 \
-Dscala.minor.version=2.10.5 \
-Dspark.version=1.5.2
```

## build profiles

Alternatively one can build against a limited number of pre-defined profiles.
See the [pom](pom.xml) for a list of the profiles.

Example build with profiles: 

`mvn clean package -Pspark_2.3,scala_2.11`

`mvn clean package -Pspark_2.0,scala_2.10`

`mvn clean package -Pspark_1.6,scala_2.11`

# Support

Here is a handy table of supported build version combinations:

| Apache Spark | Scala |
|:------------:|:-----:|
| 1.5.x        | 2.10  |
| 1.5.x        | 2.11  |
| 1.6.x        | 2.10  |
| 1.6.x        | 2.11  |
| 2.0.x        | 2.10  |
| 2.0.x        | 2.11  | 
| 2.1.x        | 2.10  |
| 2.1.x        | 2.11  |
| 2.2.x        | 2.10  |
| 2.2.x        | 2.11  |
| 2.3.x        | 2.11  |
