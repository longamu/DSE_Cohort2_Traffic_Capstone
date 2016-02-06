package org.ucsd.dse.capstone.anomaly

import org.apache.spark.SparkContext
import org.apache.spark.sql.SQLContext
import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.rdd.RDD

import org.ucsd.dse.capstone.traffic._ /*DefaultSparkTemplate
import org.ucsd.dse.capstone.traffic.Fields
import org.ucsd.dse.capstone.traffic.PivotHandler
import org.ucsd.dse.capstone.traffic.SparkTemplate
import org.ucsd.dse.capstone.traffic.StandardPivotHandler
import org.ucsd.dse.capstone.traffic.MLibUtils*/

object AnomalyMain {
    private def parseVector(line: String): Vector =
    {
        Vectors.dense(line.split(',').map(_.toDouble))
    }

    def main(args: Array[String])
    {
        val template: SparkTemplate = new DefaultSparkTemplate()
        template.execute { sc => do_execute(sc) }
    }

    def do_execute(sc: SparkContext)
    {
        /*val files:List[String] = List("station_5min/d11_text_station_5min_2010_01_*.txt.gz")
        val m_string_rdd:RDD[String] = sc.textFile(files.mkString(","))*/
        
        val files:List[String] = List("/Users/johngill/Documents/DSE/jgilliii/dse_capstone/ml/notebooks/t_data.csv")
        val data_rdd:RDD[(Array[Int], Vector)] = sc.textFile(files.mkString(",")).zipWithIndex().map{ case(o,i) => (Array(i.toInt), parseVector(o)) }
        
//        val sqlContext: SQLContext = new SQLContext(sc)
//        val path = "data location"
//        val data_rdd: RDD[(Array[Int], Vector)] = IOUtils.toVectorRDD_withKeys(IOUtils.read_pivot_df(sc, sqlContext, path), PivotColumnPrefixes.SPEED)
        
        val mahala:AnomalyDetector = new MahalanobisOutlier(sc)
        mahala.fit(data_rdd.map{x => x._2})
        val m_rslts = mahala.DetectOutlier(data_rdd, 1.0).collect()
        println(m_rslts.length)
        m_rslts.foreach { r => println(r._1.deep.mkString(",") + " " + r._2) }
        
        val kmeans:AnomalyDetector = new KMeansOutlier(sc, 5, 10)
        kmeans.fit(data_rdd.map{x => x._2})
        kmeans.DetectOutlier(data_rdd, 0.1).collect().foreach { r => println(r._1.deep.mkString(",") + " " + r._2) }
    }
}