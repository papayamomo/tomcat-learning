package ex02.pyrmont;

import java.io.IOException;

public class StaticResourceProcessor {

	public void process(Request reuqest, Response response) {
		try {
			response.sendStaticResource();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
