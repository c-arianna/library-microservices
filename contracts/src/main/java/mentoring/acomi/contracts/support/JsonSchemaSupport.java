package mentoring.acomi.contracts.support;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import com.networknt.schema.InputFormat;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.SpecificationVersion;

public abstract class JsonSchemaSupport {
	
	protected InputStream loadResource(String path) {

		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);

		if (is == null) {
			throw new IllegalStateException(String.format("File not found in classpath: %s", path));
		}

		return is;
	}

    protected Schema loadSchema(String path) {

        SchemaRegistry registry =
            SchemaRegistry.withDefaultDialect(
                SpecificationVersion.DRAFT_2020_12,
                builder -> builder.schemas(iri -> {
                    try (InputStream is = loadResource(iri)) {
                        return new String(
                            is.readAllBytes(),
                            StandardCharsets.UTF_8
                        );
                    } catch (Exception ex) {
                        return null;
                    }
                })
            );

        return registry.getSchema(SchemaLocation.of(path), loadResource(path), InputFormat.JSON);
        
    }

}
