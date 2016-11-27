package org.raliclo.apache.mapreduce_Q1;

import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import java.nio.file.Files;
import java.nio.file.Paths;

public class SectionsByCourse {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {

        if (args.length == 0) {
            args = new String[2];
            args[0] = Paths.get("").toAbsolutePath().toString()
                    .concat("/src/main/java/org/raliclo/apache/mapreduce_Q1/input");
            args[1] = Paths.get("").toAbsolutePath().toString()
                    .concat("/src/main/java/org/raliclo/apache/mapreduce_Q1/output");
        }

        java.nio.file.Path path = Paths.get(args[1]);
        if (Files.exists(path)) {
            FileUtils.deleteDirectory(path.toFile());
        }

        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "Q3MainJob");

        job.setJarByClass(SectionsByCourse.class);
        job.setMapperClass(SectionsByCourseMapper.class);
        job.setReducerClass(SectionsByCourseReducer.class);

//        job.setCombinerClass(SectionsByCourseCombiner_2ndExample.class);
//        job.setReducerClass(SectionsByCourseReducer_2ndExample.class);

        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));


        System.exit(job.waitForCompletion(true) ? 0 : 1);

    }

}