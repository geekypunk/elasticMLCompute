import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

public class HDFSTest {

	public static void main(String[] args) {
		Configuration configuration = new Configuration();
		FileSystem hdfs;
		try {
			hdfs = FileSystem.get( new URI( "hdfs://localhost:9000" ), configuration );
			Path file = new Path("hdfs://localhost:9000/data/a.txt");
			BufferedWriter br=new BufferedWriter(new OutputStreamWriter(hdfs.create(file,true)));
			String line;
			line="Disha Dishu Daasha";
			System.out.println(line);
			br.write(line);
			br.close();
			hdfs.close();
		} catch (IOException | URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

	}

}
