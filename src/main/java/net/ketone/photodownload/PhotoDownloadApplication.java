package net.ketone.photodownload;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.glassfish.jersey.client.ClientProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Map;


@SpringBootApplication
public class PhotoDownloadApplication implements CommandLineRunner {

	@Autowired
	private CookieStore cookieStore;
	@Value("${photo.url}")
	private String photoUrl;
	@Value("${auth.url}")
	private String url;
	@Value("${auth.username}")
	private String username;
	@Value("${auth.password}")
	private String password;

	public static void main(String[] args) {
		SpringApplication.run(PhotoDownloadApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {

		// authenticate
		Client authClient = ClientBuilder.newClient().register(cookieStore);

		WebTarget baseTarget = authClient.target(url);
		System.out.println("Authenticating." + url);
		Form form = new Form();
		form.param("username", username);
		form.param("password", password);
		form.param("login", "Submit");
		Response response = baseTarget
				.request("text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
				.property(ClientProperties.FOLLOW_REDIRECTS, Boolean.TRUE)
				.post(Entity.form(form));

		Map<String, NewCookie> cr2 = response.getCookies();
		System.out.println("Response=" + response.getStatus());


		System.out.println("getting photo url=" + photoUrl);

		// photos are with ids 881 to 1031
		for(int i = 881; i <= 1031; i++) {
			Client client = ClientBuilder.newClient().register(cookieStore);
			Response resp = client
					.target(photoUrl)
					.queryParam("id", i)
					.queryParam("part", "e")
					.queryParam("download", null)
					.request("text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng")
					.get();
			System.out.println("photo=" + i + " status=" + resp.getStatus());

			InputStream is = resp.readEntity(InputStream.class);
			fetchFeed(is, i);
			//fetchFeedAnotherWay(is) //use for Java 7
			IOUtils.closeQuietly(is);
		}
	}

	/**
	 * Store contents of file from response to local disk using java 7
	 * java.nio.file.Files
	 */
	private void fetchFeed(InputStream is, int i) throws IOException {
		File downloadfile = new File(i + ".jpg");
		Files.copy(is, downloadfile.toPath(), StandardCopyOption.REPLACE_EXISTING);
	}
}
