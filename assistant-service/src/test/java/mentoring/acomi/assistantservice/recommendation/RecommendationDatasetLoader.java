package mentoring.acomi.assistantservice.recommendation;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Component;

import mentoring.acomi.assistantservice.application.view.BookView;
import tools.jackson.databind.ObjectMapper;

@Component
public class RecommendationDatasetLoader {

	private final ObjectMapper objectMapper;

	public RecommendationDatasetLoader(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public List<BookView> load() {

		try (InputStream is = RecommendationDatasetLoader.class.getResourceAsStream("/dataset/book-catalog-dataset.json")) {

			if (is == null) {
                throw new IllegalStateException("File book-catalog-dataset.json not found in src/test/resources");
            }
			
			return Arrays.asList(objectMapper.readValue(is, BookView[].class));

		} catch (IOException e) {
			throw new IllegalStateException("Error reading book catalog dataset", e);
		}
	}
}