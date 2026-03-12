package de.binaerebauten.gleichklang.core.migration;

import de.binaerebauten.gleichklang.core.config.PersistenceConfig;
import de.binaerebauten.gleichklang.core.utils.ImageUtil;
import de.binaerebauten.gleichklang.core.utils.PreparedStatmentSetterBuilder;
import org.flywaydb.core.api.configuration.ConfigurationAware;
import org.flywaydb.core.api.configuration.FlywayConfiguration;
import org.flywaydb.core.api.migration.spring.SpringJdbcMigration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Base64;
import java.util.Objects;

public class V02_82__Migrate_Images implements PreparedStatmentSetterBuilder, SpringJdbcMigration, ConfigurationAware
{
	private static final int BATCH_SIZE = 100;
	private static final Logger LOG = LoggerFactory.getLogger(V02_82__Migrate_Images.class);
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
		// if(filePath == null) return;
		
		resetAll(jdbcTemplate);
		migrateAvatar(jdbcTemplate, PARTNERSHIP, OLD_PARTNERSHIP_TABLE);
		migrateAvatar(jdbcTemplate, FRIENDSHIP, OLD_FRIENDSHIP_TABLE);
		migrateGallery(jdbcTemplate, PARTNERSHIP, OLD_PARTNERSHIP_TABLE);
		migrateGallery(jdbcTemplate, FRIENDSHIP, OLD_FRIENDSHIP_TABLE);
	}
	
	private void migrateGallery(JdbcTemplate jdbcTemplate, String category, String compTableName) throws Exception
	{
		int offset = 0;
		int count;
		do
		{
			final SqlRowSet rowSet = jdbcTemplate.queryForRowSet("SELECT c.foto1 AS foto1, c.foto2 AS foto2, c.foto3 AS foto3, u.id AS id FROM " +
					compTableName +
					" c, user_ u WHERE c.owner = u.legacy_id AND c.hide_fotos = 0 AND (c.foto2 IS NOT NULL OR c.foto3 IS NOT NULL) ORDER BY u.id LIMIT " +
					offset + ", " + BATCH_SIZE + ";");
			
			count = 0;
			while (rowSet.next())
			{
				count++;
				
				final Long userId = rowSet.getLong("id");
				jdbcTemplate.update("INSERT INTO media_gallery (name, author_id, visible_category, deleted, create_date) VALUES (CONCAT('Migrate ', ?), ?, ?, false, NOW());",
						setParams(category, userId, category));
				
				final Long mediaGalleryId = jdbcTemplate.queryForObject("SELECT id FROM media_gallery WHERE id = LAST_INSERT_ID();", Long.class);
				
				final boolean r1 = migrateMedia(jdbcTemplate, rowSet, mediaGalleryId, "foto1");
				final boolean r2 = migrateMedia(jdbcTemplate, rowSet, mediaGalleryId, "foto2");
				final boolean r3 = migrateMedia(jdbcTemplate, rowSet, mediaGalleryId, "foto3");
				
				if(!r1 && !r2 && !r3)
				{
					jdbcTemplate.update("DELETE FROM media_gallery WHERE id = ?", setParams(mediaGalleryId));
				}
			}
			
			offset += BATCH_SIZE;
		} while (count == BATCH_SIZE);
	}
	
	private boolean migrateMedia(JdbcTemplate jdbcTemplate, SqlRowSet rowSet, Long mediaGalleryId, String column)
	{
		if (!rowSet.wasNull())
		{
			final String filename = "migrate_foto.jpg";
			final Object obj = rowSet.getObject(column);
			if (Objects.nonNull(obj))
			{
				jdbcTemplate.update("INSERT INTO file (type, name, create_date) VALUES ('IMAGE', ?, NOW());", setParams(filename));
				jdbcTemplate.update("INSERT INTO media (file_id, media_gallery_id, deleted, create_date) VALUES (LAST_INSERT_ID(), ?, false, NOW());", setParams(mediaGalleryId));
				final Long fileId = jdbcTemplate.queryForObject("SELECT file_id FROM media WHERE id = LAST_INSERT_ID();", Long.class);
				if(!persistFile(fileId, obj, filename))
				{
					jdbcTemplate.update("DELETE FROM media WHERE file_id = ?", setParams(fileId));
					jdbcTemplate.update("DELETE FROM file WHERE id = ?", setParams(fileId));
					return false;
				}
				return true;
			}
		}
		return false;
	}
	
	private void migrateAvatar(JdbcTemplate jdbcTemplate, String category, String compTableName) throws Exception
	{
		final String filename = "migrate_avatar.jpg";
		
		int offset = 0;
		int count;
		do
		{
			final SqlRowSet rowSet = jdbcTemplate.queryForRowSet("SELECT c.foto1 AS foto1, u.id AS id FROM " +
					compTableName +
					" c, user_ u WHERE c.owner = u.legacy_id AND c.hide_fotos = 0 AND c.foto1 IS NOT NULL ORDER BY u.id LIMIT " +
					offset + ", " + BATCH_SIZE + ";");
			
			count = 0;
			while (rowSet.next())
			{
				count++;
				
				final Object foto1 = rowSet.getObject("foto1");
				final Long userId = rowSet.getLong("id");
				
				jdbcTemplate.update("INSERT INTO file (type, name, create_date) VALUES ('IMAGE', ?, NOW());", setParams(filename));
				jdbcTemplate.update("INSERT INTO avatar (category, file_id, user_id, create_date) VALUES (?, LAST_INSERT_ID(), ?, NOW());", setParams(category, userId));
				
				final Long fileId = jdbcTemplate.queryForObject("SELECT file_id FROM avatar WHERE id = LAST_INSERT_ID();", Long.class);
				if(!persistFile(fileId, foto1, filename))
				{
					jdbcTemplate.update("DELETE FROM avatar WHERE file_id = ?", setParams(fileId));
					jdbcTemplate.update("DELETE FROM file WHERE id = ?", setParams(fileId));
				}
			}
			
			offset += BATCH_SIZE;
		} while (count == BATCH_SIZE);
	}
	
	private boolean persistFile(Long id, Object base64image, String fileName)
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
			return false;
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
			return false;
		}
		
		return true;
	}
	
	private Path getFilePath(String... more)
	{
		// final String filePath = PropertiesLoader.getProperties("application.properties").getProperty("file.path");
		return Paths.get(filePath, more);
	}
	
	private void resetAll(JdbcTemplate jdbcTemplate)
	{
		jdbcTemplate.execute("DELETE FROM media;");
		jdbcTemplate.execute("DELETE FROM media_gallery;");
		jdbcTemplate.execute("DELETE FROM avatar;");
		jdbcTemplate.execute("DELETE FROM file;");
		
		try
		{
			final Path filePath = getFilePath();
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
					if (!filePath.equals(dir))
					{
						try
						{
							Files.delete(dir);
						}
						catch (IOException ignored)
						{
						}
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
