package de.binaerebauten.gleichklang.core.migration;

import de.binaerebauten.gleichklang.core.config.PersistenceConfig;
import de.binaerebauten.gleichklang.core.migration.model.AvatarInfo;
import de.binaerebauten.gleichklang.core.model.media.Avatar;
import de.binaerebauten.gleichklang.core.utils.ImageUtil;
import de.binaerebauten.gleichklang.core.utils.PreparedStatmentSetterBuilder;
import org.flywaydb.core.api.configuration.ConfigurationAware;
import org.flywaydb.core.api.configuration.FlywayConfiguration;
import org.flywaydb.core.api.migration.spring.SpringJdbcMigration;
import org.jfree.util.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.FileImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;

/**
 * Created by rgoerner on 16.05.17.
 */
public class V04_01__Create_Thumbnails_From_Avatars implements PreparedStatmentSetterBuilder, SpringJdbcMigration, ConfigurationAware
{
    private static final Logger LOG = LoggerFactory.getLogger(V04_01__Create_Thumbnails_From_Avatars.class);
    private String filePath;

    @Override
    public void setFlywayConfiguration(FlywayConfiguration flywayConfiguration)
    {
        filePath = flywayConfiguration.getPlaceholders().get(PersistenceConfig.PROPERTY_FILE_PATH);
    }

    @Override
    public void migrate(JdbcTemplate jdbcTemplate) throws Exception
    {
        createThumbnailsFromAvatar(jdbcTemplate);
    }


    private void createThumbnailsFromAvatar(JdbcTemplate jdbcTemplate)
    {

        jdbcTemplate.execute("CALL ADD_COLUMN('avatar', 'thumbnail_id', 'BIGINT')");
        jdbcTemplate.execute("CALL ADD_FOREIGN_KEY('avatar', 'thumbnail_id', 'file', 'id')");

        // commented out because creation of thumbnails may have a strong influence on duration of migration

//        final List<AvatarInfo> avatarInfos = jdbcTemplate.query("SELECT a.id, a.file_id, f.name, a.user_id FROM avatar a JOIN file f on a.file_id = f.id where a.thumbnail_id IS NULL",
//                new BeanPropertyRowMapper<>(AvatarInfo.class));
//
//        LOG.info("Found " +avatarInfos.size() +" avatars without thumbnail");
//
//        for (AvatarInfo a : avatarInfos)
//        {
//            jdbcTemplate.update("INSERT INTO file (type, name, create_date) VALUES ('IMAGE', ?, NOW());", setParams(a.getName()));
//
//            final Long fileId = jdbcTemplate.queryForObject("SELECT id FROM file WHERE id = LAST_INSERT_ID();", Long.class);
//
//            jdbcTemplate.update("UPDATE avatar SET thumbnail_id = LAST_INSERT_ID() WHERE id = ?;", setParams(a.getId()));
//
//            if (a.getFileId() != null && a.getName() != null)
//            {
//                final Path targetDirectory = getFilePath(a.getFileId().toString());
//                final Path filePath = targetDirectory.resolve(a.getName());
//
//                BufferedImage image = null;
//                try
//                {
//                    image = ImageIO.read(new File(filePath.toString()));
//                }
//                catch (IOException e)
//                {
//                    Log.error("Cannot read file " +fileId + "/"+filePath);
//                }
//
//                if (image != null)
//                {
//                    persistFile(fileId, image, a.getName());
//                }
//                else
//                    Log.error("image is null " +fileId + "/"+filePath);
//            }
//            else
//            {
//                Log.error("image information not complete");
//            }
//        }

    }

    private void persistFile(Long id, BufferedImage image, String fileName)
    {
        final Path targetDirectory = getFilePath(id.toString());
        final Path filePath = targetDirectory.resolve(fileName);

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
            final BufferedImage scaled = ImageUtil.scaleImage(image, 100, 100);

            JPEGImageWriteParam jpegParams = new JPEGImageWriteParam(null);
            jpegParams.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            jpegParams.setCompressionQuality(0.85f);
            jpegParams.setProgressiveMode(ImageWriteParam.MODE_DEFAULT);

            final ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
            writer.setOutput(new FileImageOutputStream(
                    new File(targetDirectory.toString())));

            writer.write(null, new IIOImage(scaled, null, null), jpegParams);
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


}
