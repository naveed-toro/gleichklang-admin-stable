package de.binaerebauten.gleichklang.core.migration;

import de.binaerebauten.gleichklang.core.config.PersistenceConfig;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.utils.ImageUtil;
import de.binaerebauten.gleichklang.core.utils.PreparedStatmentSetterBuilder;
import org.flywaydb.core.api.configuration.ConfigurationAware;
import org.flywaydb.core.api.configuration.FlywayConfiguration;
import org.flywaydb.core.api.migration.spring.SpringJdbcMigration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Base64;

public class V03_05__Migrate_Secret_Images implements PreparedStatmentSetterBuilder, SpringJdbcMigration, ConfigurationAware
{
	private static final int BATCH_SIZE = 100;
	private static final Logger LOG = LoggerFactory.getLogger(V03_05__Migrate_Secret_Images.class);
	private String filePath;
	
	private static final String PARTNERSHIP = "PARTNERSHIP";
	private static final String FRIENDSHIP = "FRIENDSHIP";
	
	private static final String OLD_PARTNERSHIP_TABLE = "comppfoto";
	private static final String OLD_FRIENDSHIP_TABLE = "compffoto";

	@Override
	public void setFlywayConfiguration(FlywayConfiguration flywayConfiguration)
	{
		filePath = flywayConfiguration.getPlaceholders().get(PersistenceConfig.PROPERTY_FILE_PATH);
	}

	@Override
	public void migrate(JdbcTemplate jdbcTemplate) throws Exception
	{
		resetAll(jdbcTemplate);
		migrateGallery(jdbcTemplate, PARTNERSHIP, OLD_PARTNERSHIP_TABLE);
		migrateGallery(jdbcTemplate, FRIENDSHIP, OLD_FRIENDSHIP_TABLE);
	}

	private void migrateGallery(JdbcTemplate jdbcTemplate, String category, String compTableName) throws Exception
	{
		int offset = 0;
		int count;
		do
		{
			final SqlRowSet rowSet = jdbcTemplate.queryForRowSet("SELECT c.foto1 AS foto1, c.foto2 AS foto2, c.foto3 AS foto3, u.id AS id FROM " + compTableName + " c, user_ u WHERE c.owner = u.legacy_id AND c.hide_fotos = 1 ORDER BY u.id LIMIT " + offset + ", " + BATCH_SIZE + ";");

			count = 0;
			while (rowSet.next())
			{
				count++;

				final Long userId = rowSet.getLong("id");
				jdbcTemplate.update("INSERT INTO media_gallery (name, author_id, visible_category, secret, deleted, create_date) VALUES (CONCAT('Secret Migrate ', ?), ?, ?, true, false, NOW());",
						setParams(category, userId, category));
				final Long mediaGalleryId = jdbcTemplate.queryForObject("SELECT id FROM media_gallery WHERE id = LAST_INSERT_ID();", Long.class);

				migrateVisibleRelationship(jdbcTemplate, mediaGalleryId, userId);

				final Object foto1 = rowSet.getObject("foto1");
				if (!rowSet.wasNull())
				{
					migrateMedia(jdbcTemplate, mediaGalleryId, foto1);
				}
				final Object foto2 = rowSet.getObject("foto2");
				if (!rowSet.wasNull())
				{
					migrateMedia(jdbcTemplate, mediaGalleryId, foto2);
				}
				final Object foto3 = rowSet.getObject("foto3");
				if (!rowSet.wasNull())
				{
					migrateMedia(jdbcTemplate, mediaGalleryId, foto3);
				}
			}

			offset += BATCH_SIZE;

			commit(jdbcTemplate);
		} while (count == BATCH_SIZE);
	}

	private void commit(JdbcTemplate jdbcTemplate)
	{
		ConnectionCallback commitAction	= c -> {
			c.commit();
			return null;
		};

		long start = System.currentTimeMillis();

		jdbcTemplate.execute(commitAction);
		long end = System.currentTimeMillis();

		LOG.info("Successfully commited transaction. It took {} ms", String.valueOf(end - start));
	}

	private String getCompTableName(RecommendationCategory category) throws Exception
	{
		final String compTableName;
		switch (category)
		{
			case FRIENDSHIP:
				compTableName = "compffoto";
				break;
			case PARTNERSHIP:
				compTableName = "comppfoto";
				break;
			default:
				throw new Exception("not possible");
		}
		return compTableName;
	}

	private void migrateMedia(JdbcTemplate jdbcTemplate, Long mediaGalleryId, Object foto)
	{
		final String filename = "migrate_foto.jpg";
		jdbcTemplate.update("INSERT INTO file (type, name, create_date) VALUES ('IMAGE', ?, NOW());", setParams(filename));
		jdbcTemplate.update("INSERT INTO media (file_id, media_gallery_id, deleted, create_date) VALUES (LAST_INSERT_ID(), ?, false, NOW());", setParams(mediaGalleryId));
		final Long fileId = jdbcTemplate.queryForObject("SELECT file_id FROM media WHERE id = LAST_INSERT_ID();", Long.class);
		persistFile(fileId, foto, filename);
	}

	private void migrateVisibleRelationship(JdbcTemplate jdbcTemplate, Long mediaGalleryId, Long userId)
	{
		jdbcTemplate.update
				(
						"INSERT INTO media_gallery_visible_relationship (media_gallery_id, relationship_id) " +
								"SELECT ?, r.id " +
								"FROM relationship r WHERE r.source_user_id = ? AND " +
								"EXISTS(SELECT 1 FROM compvorschlag cv WHERE cv.no = r.legacy_id AND cv.visibility_fotos = 'show');",
						setParams(mediaGalleryId, userId)
				);
	}

	private void persistFile(Long id, Object base64image, String fileName)
	{
		final byte[] image;
		final String base64String = new String((byte[]) base64image);
		final String cleanBase64String = base64String.startsWith("\'") ? base64String.substring(1, base64String.length() - 1) : base64String;
		final Path targetDirectory = getFilePath(id.toString());
		final Path filePath = targetDirectory.resolve(fileName);

		try
		{
			image = Base64.getDecoder().decode(cleanBase64String);
		}
		catch (Exception ex)
		{
			LOG.error("Base64 decode failed", ex);
			return;
		}

		try
		{
			Files.createDirectories(targetDirectory);
		}
		catch (IOException ex)
		{
			LOG.error("could not create directory", ex);
			throw new RuntimeException(ex);
		}

		try
		{
			ImageUtil.convertImage(image, filePath, 400, 400);
		}
		catch (IOException ex)
		{
			LOG.error("Image convert failed", ex);
			return;
		}
	}

	private Path getFilePath(String... more)
	{
		return Paths.get(filePath, more);
	}

	private void resetAll(JdbcTemplate jdbcTemplate)
	{
		final SqlRowSet rowSet = jdbcTemplate.queryForRowSet("SELECT m.file_id as file_id FROM media m LEFT JOIN media_gallery mg ON m.media_gallery_id = mg.id WHERE mg.secret = true;");

		while (rowSet.next())
		{
			final Long userId = rowSet.getLong("file_id");
			deletePath(getFilePath(userId.toString()));
		}

		jdbcTemplate.execute("DELETE FROM media_gallery_visible_relationship");
		jdbcTemplate.execute("DELETE m FROM media m LEFT JOIN media_gallery mg ON m.media_gallery_id = mg.id WHERE mg.secret = true;");
		jdbcTemplate.execute("DELETE FROM media_gallery WHERE secret = true;");
		jdbcTemplate.execute("DELETE f FROM file f WHERE "
				+ "NOT EXISTS (SELECT * FROM media m WHERE m.file_id = f.id) AND "
				+ "NOT EXISTS (SELECT * FROM avatar a WHERE a.file_id = f.id) AND "
				+ "NOT EXISTS (SELECT * FROM message_attachment ma WHERE ma.file_id = f.id);");

		commit(jdbcTemplate);
	}

	private void deletePath(Path filePath)
	{
		try
		{
			Files.walkFileTree(filePath, new SimpleFileVisitor<Path>()
			{
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
				{
					try
					{
						Files.delete(file);
					}
					catch (IOException ignored)
					{
					}
					return FileVisitResult.CONTINUE;
				}

				public FileVisitResult visitFileFailed(Path file, IOException exc)
				{
					// try to delete the file anyway, even if its attributes
					// could not be read, since delete-only access is
					// theoretically possible
					try
					{
						Files.delete(file);
					}
					catch (IOException ignored)
					{
					}
					return FileVisitResult.CONTINUE;
				}

				public FileVisitResult postVisitDirectory(Path dir, IOException exc)
				{
					try
					{
						Files.delete(dir);
					}
					catch (IOException ignored)
					{
					}
					return FileVisitResult.CONTINUE;
				}
			});
		}
		catch (IOException ignored)
		{
		}
	}
}
