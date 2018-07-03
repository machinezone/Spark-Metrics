/*
 * Copyright (c) 2018 Machine Zone Inc. All rights reserved.
 */
package org.apache.spark.metrics.mz

import java.util.concurrent.TimeUnit

import com.codahale.metrics
import org.apache.spark.{SparkConf, SparkContext}
import org.scalatest._

/**
  * CustomMetricsTest
  *
  * Unit tests for [[CustomMetrics]].
  *
  * @author belbis
  * @since 1.0.0
  */
class CustomMetricsTest extends FlatSpec with Matchers with BeforeAndAfter {

  private val host = "localhost"
  private val master = "local[2]"
  private val appName = "test"

  private lazy val sc: SparkContext = {
    val conf = new SparkConf()
      .set("spark.driver.host", host)
      .setMaster(master)
      .setAppName(appName)
    new SparkContext(conf)
  }

  private lazy val custom = new CustomMetrics("custom")
  
  private lazy val registry = sc.env.metricsSystem.getSourcesByName("custom").head.metricRegistry

  before {
    sc
  }

  after {
    if (sc != null) sc.stop
  }

  "#counter()" should "create a counter" in {
    val cntr = custom.counter("cntr")
    cntr.isInstanceOf[metrics.Counter] should equal(true)
    registry.getCounters.get("cntr") should equal(cntr)
  }

  "#gauge()" should "create a gauge" in {
    var i = 0
    val f = () => {
      i += 1
      i
    }
    val gauge = custom.gauge[Int]("gauge", f)
    gauge.getValue should equal(1)
    gauge.getValue should equal(2)
    gauge.isInstanceOf[metrics.Gauge[Int]] should equal(true)
    registry.getGauges.get("gauge") should equal(gauge)
  }

  "#histogram()" should "create a histogram" in {
    val histo = custom.histogram("histo")
    histo.update(15)
    histo.getCount should equal(1)
    histo.getSnapshot.getMin should equal(15)
    histo.isInstanceOf[metrics.Histogram] should equal(true)
    registry.getHistograms.get("histo") should equal(histo)
  }

  "#meter()" should "create a meter" in {
    val meter = custom.meter("meter")
    meter.isInstanceOf[metrics.Meter] should equal(true)
    registry.getMeters.get("meter") should equal(meter)
  }

  "#timer()" should "create a timer" in {
    val tmr = custom.timer("tmr")
    tmr.update(4, TimeUnit.SECONDS)
    tmr.getCount should equal(1)
    tmr.getSnapshot.getMax should equal(4000000000L)
    tmr.isInstanceOf[metrics.Timer] should equal(true)
    registry.getTimers.get("tmr") should equal(tmr)
  }

  "#remove()" should "remove an existing metric" in {
    val m = custom.meter("met")
    registry.getMeters.get("met") should equal(m)
    custom.remove("met") should equal(true)
    registry.getMeters.containsKey("met") should equal(false)
  }

}
