package test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;

public class DownloadFile {

	public static void main(String[] args) throws IOException, InterruptedException {

		String url = "https://www.pianetafanta.it/api/voti/export" + "?variante=ufficiali" + "&stagione=2026_2027"
				+ "&bonusTipo=standard" + "&giornata=4";

		Path output = Path.of("voti_giornata_4");

		HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();

		HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url))
				.header("User-Agent",
						"Mozilla/5.0 (Windows NT 10.0; Win64; x64) " + "AppleWebKit/537.36 " + "(KHTML, like Gecko) "
								+ "Chrome/140.0.0.0 Safari/537.36")
				.header("Accept",
						"text/html,application/xhtml+xml,application/xml;" + "q=0.9,image/avif,image/webp,*/*;q=0.8")
				.header("Referer", "https://www.pianetafanta.it/").GET().build();

		HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());

//		System.out.println("HTTP status: " + response.statusCode());

		if (response.statusCode() >= 200 && response.statusCode() < 300) {

			Files.write(output, response.body());

//			System.out.println("File scaricato: " + output.toAbsolutePath());

		} else {
//			System.out.println(new String(response.body()));

			throw new IOException("Download fallito. HTTP status: " + response.statusCode());
		}
	}
}
