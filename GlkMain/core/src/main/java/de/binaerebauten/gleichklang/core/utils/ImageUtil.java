package de.binaerebauten.gleichklang.core.utils;

import com.vaadin.server.StreamResource;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.FileImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Path;

public class ImageUtil
{
	public static final String JPG_FORMAT = "jpg";
	
	private ImageUtil()
	{
	}

	private static BufferedImage convertImage(BufferedImage image, int maxHeight, int maxWidth) throws IOException
	{
		int newHeight = image.getHeight();
		int newWidth = image.getWidth();

		if (newHeight > maxHeight)
		{
			newHeight = maxHeight;
			newWidth = (newHeight * image.getWidth()) / image.getHeight();
		}

		if (newWidth > maxWidth)
		{
			newWidth = maxWidth;
			newHeight = (newWidth * image.getHeight()) / image.getWidth();
		}

		final int type = BufferedImage.TYPE_INT_RGB;

		final BufferedImage convertedImage = new BufferedImage(newWidth, newHeight, type);
		final Graphics2D g2d = convertedImage.createGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		g2d.drawImage(image, 0, 0, newWidth, newHeight, Color.WHITE, null);
		g2d.dispose();

		return convertedImage;
	}

	//only for use in migration
	public static void convertImage(byte[] imageData, Path targetPath, int maxHeight, int maxWidth) throws IOException
	{
		ByteArrayInputStream bais = new ByteArrayInputStream(imageData);
		final BufferedImage image = ImageIO.read(bais);

		// not an image
		if (image == null) throw new IOException("not an image");

		ImageIO.write(convertImage(image, maxHeight, maxWidth), JPG_FORMAT, targetPath.toFile());

	}

	public static void convertImage(Path path, int maxHeight, int maxWidth) throws IOException
	{
		final BufferedImage image = ImageIO.read(path.toFile());

		// not an image
		if (image == null) throw new IOException("not an image");

		JPEGImageWriteParam jpegParams = new JPEGImageWriteParam(null);
		jpegParams.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
		jpegParams.setCompressionQuality(0.95f);
		jpegParams.setProgressiveMode(ImageWriteParam.MODE_DEFAULT);

		final ImageWriter writer = ImageIO.getImageWritersByFormatName(JPG_FORMAT).next();
		writer.setOutput(new FileImageOutputStream(
				new File(path.toString())));

		writer.write(null, new IIOImage(convertImage(image, maxHeight, maxWidth), null, null), jpegParams);

	}

	public static BufferedImage scaleImage(BufferedImage image, int maxHeight, int maxWidth) throws IOException
	{
		return convertImage(image, maxHeight, maxWidth);
	}

	public static BufferedImage rotate90(BufferedImage imageToRotate)
	{

		int w = Math.round(imageToRotate.getWidth());
		int h = Math.round(imageToRotate.getHeight());

		final BufferedImage rotatedImage = new BufferedImage( h, w , imageToRotate.getType());

		for( int x = 0; x < w; x++ )
		{
			for( int y = 0; y < h; y++ )
			{
				rotatedImage.setRGB(y, w - x - 1, imageToRotate.getRGB( x, y  )  );
			}
		}

		return rotatedImage;
	}

	public static StreamResource createStreamResource(BufferedImage image, String path)
	{
		return new StreamResource(new StreamResource.StreamSource()
		{
			@Override
			public InputStream getStream()
			{

				try
				{
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					ImageIO.write(image, JPG_FORMAT, bos);
					return new ByteArrayInputStream(bos.toByteArray());
				}
				catch (IOException e)
				{
					e.printStackTrace();
					return null;
				}
			}
		}, path);
	}
}
