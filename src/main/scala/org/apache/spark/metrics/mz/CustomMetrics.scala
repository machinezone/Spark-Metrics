/*
 * Copyright (c) 2018 Machine Zone Inc. All rights reserved.
 */
package org.apache.spark.metrics.mz

import com.codahale.metrics._
import org.apache.spark.SparkEnv
import org.apache.spark.metrics.MetricsSystem
import org.apache.spark.metrics.source.Source
import org.slf4j.LoggerFactory

import scala.util.{Failure, Success, Try}

/**
  * CustomMetrics
  *
  * Provide access to Spark's internal [[MetricRegistry]].
  *
  * @author belbis
  * @since 1.0.0
  */
class CustomMetrics(override val sourceName: String) extends Source {

  private val _log = LoggerFactory.getLogger(getClass)

  val registry = new MetricRegistry()

  override def metricRegistry: MetricRegistry = registry

  /**
    * metrics
    *
    * This is our reference to the Spark internal [[MetricsSystem]].
    * If loading the metrics system from [[SparkEnv]] is unsuccessful
    * the application will fail.
    *
    */
  @transient lazy val metrics: MetricsSystem = {
    Try(SparkEnv.get) match {
      case Success(null) =>
        throw new Exception("Unable to find metrics system.")
      case Success(env) => env.metricsSystem
      case Failure(f) =>
        _log.error("Unable to access metrics system.")
        throw f
    }
  }

  /**
    * register
    *
    * Ensure that this as a source is registered with
    * the Spark [[MetricsSystem]]. The cycle parameter is used to ensure the
    * [[Source]] is registered with updated metrics.
    *
    * @param cycle
    */
  def register(cycle: Boolean = false): Unit = {
    if (metrics.getSourcesByName(sourceName).isEmpty) {
      metrics.registerSource(this)
    } else if (cycle) {
      metrics.removeSource(this)
      metrics.registerSource(this)
      _log.info("Successfully ensured CustomMetrics registered with Spark's MetricsSystem")
    }
  }

  /**
    * register
    *
    * register a named metric in the [[MetricRegistry]].
    *
    * @param name [[String]] name of [[Metric]] to register
    * @param metric [[Metric]] to register
    */
  def register[T <: Metric](name: String, metric: T): Unit = {
    metricRegistry.register(name, metric)
    register(cycle = true)
  }

  /**
    * counter
    *
    * Get [[Counter]] registered with specified name.
    * If one doesn't exist it will be created.
    *
    * @param name the name of the counter
    * @return [[Counter]]
    *
    */
  def counter(name: String): Counter = {
    if (metricRegistry.getCounters.containsKey(name)) {
      metricRegistry.getCounters.get(name)
    } else synchronized {
      val cycle = !metricRegistry.getCounters.containsKey(name)
      val counter = metricRegistry.counter(name)
      register(cycle)
      counter
    }
  }

  /**
    * gauge
    *
    * Register a [[Gauge]] with specified name.
    * If one exists under the same name it will be overwritten.
    *
    * @param name the name of the [[Gauge]]
    * @param f [[Function]] to return value from [[Gauge]]
    * @tparam T return type of [[Function]] for [[Gauge]]
    * @return [[Gauge]]
    *
    */
  def gauge[T](name: String, f: () => T): Gauge[T] = {
    if (metricRegistry.getGauges.containsKey(name)) {
      metricRegistry.remove(name)
    }

    synchronized {
      val gauge = new Gauge[T] {
        override def getValue: T = f()
      }
      metricRegistry.register(name, gauge)
      register(cycle = true)
      gauge
    }
  }

  /**
    * gauge
    *
    * Get [[Gauge]] registered with specified name. If one doesn't exist null is returned.
    * This behavior differs from the other [[Metric]] register implementations as
    *
    * @param name [[String]] name of [[Gauge]]
    * @return [[Gauge]]
    *
    */
  def gauge(name: String): Gauge[_] = {
    if (metricRegistry.getGauges().containsKey(name)) {
      metricRegistry.getGauges().get(name)
    } else {
      null.asInstanceOf[Gauge[_]]
    }
  }

  /**
    * histogram
    *
    * Get an [[Histogram]] registered with specified name.
    * If one doesn't exist it will be created.
    *
    * @param name [[String]] name of [[Histogram]]
    * @return [[Histogram]]
    *
    */
  def histogram(name: String): Histogram = {
    if (metricRegistry.getHistograms.containsKey(name)) {
      metricRegistry.histogram(name)
    } else synchronized {
      val cycle = !metricRegistry.getHistograms.containsKey(name)
      val histogram = metricRegistry.histogram(name)
      register(cycle)
      histogram
    }
  }

  /**
    * meter
    *
    * Get a [[Meter]] registered with specified name.
    * If one doesn't exist it will be created.
    *
    * @param name [[String]] name of [[Meter]]
    * @return [[Meter]]
    *
    */
  def meter(name: String): Meter = {
    if (metricRegistry.getMeters.containsKey(name)) {
      metricRegistry.getMeters.get(name)
    } else synchronized {
      val cycle = !metricRegistry.getMeters.containsKey(name)
      val meter = metricRegistry.meter(name)
      register(cycle)
      meter
    }
  }

  /**
    * timer
    *
    * Get a [[Timer]] registered with specified name.
    * If one doesn't exist it will be created.
    *
    * @param name [[String]] name of [[Timer]]
    * @return [[Timer]]
    *
    */
  def timer(name: String): Timer = {
    if (metricRegistry.getTimers.containsKey(name)) {
      metricRegistry.getTimers.get(name)
    } else synchronized {
      val cycle = !metricRegistry.getTimers.containsKey(name)
      val timer = metricRegistry.timer(name)
      register(cycle)
      timer
    }
  }

  /**
    * remove
    *
    * Remove a metric from the registry. Returns [[Boolean]] true if successful, false otherwise.
    * NOTE: any [[Metric]]s with a name that matches will be removed.
    *
    * @param name [[String]] name of [[Metric]] to remove.
    * @return [[Boolean]]
    *
    */
  def remove(name: String): Boolean = {
    val removed = metricRegistry.remove(name)
    register(cycle = true)
    removed
  }

}
