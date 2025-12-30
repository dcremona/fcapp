package test;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class JsoupTest{

	public static void main(String[] args) throws IOException {

		String url = "https://www.fantacalcio.it/quotazioni-fantacalcio";

		print("Fetching %s...", url);

		Document doc = Jsoup.connect(url).get();
		String title = doc.title();
		System.out.println(title);

        Elements links = doc.select("a[href]");
		Elements media = doc.select("[src]");
		Elements imports = doc.select("link[href]");

		print("\nMedia: (%d)", media.size());

        print("\nImports: (%d)", imports.size());
		for (Element link : imports) {
			print(" * %s <%s> (%s)", link.tagName(), link.attr("abs:href"), link.attr("rel"));
		}

		print("\nLinks: (%d)", links.size());
		for (Element link : links) {
			print(" * a: <%s>  (%s)", link.attr("abs:href"), trim(link.text()));
		}
	}

	private static void print(String msg, Object... args) {
		System.out.printf((msg) + "%n", args);
	}

	private static String trim(String s) {
		if (s.length() > 35) {
			return s.substring(0, 35 - 1) + ".";
		} else {
			return s;
		}
	}
}
