package com.kamtar.transport.api.utils;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.apache.commons.net.util.Base64;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class ImageUtils {

	/**
	 * Logger de la classe
	 */
	private static Logger logger = LogManager.getLogger(ImageUtils.class);  

	public static boolean saveBase64ToFile(String base64_image, String complete_path_file) {
		
		BufferedImage bimg_logo2 = null;
		String encodingPrefix = "base64,";
		if (base64_image.length() > base64_image.indexOf(encodingPrefix) + encodingPrefix.length()) {
			int contentStartIndex = base64_image.indexOf(encodingPrefix) + encodingPrefix.length();
			byte[] imageData = Base64.decodeBase64(base64_image.substring(contentStartIndex));
			if (imageData != null) {
				try {
					bimg_logo2 = ImageUtils.bytesArrayToBufferedImageWithTransparent(imageData);
					File outputfile = new File(complete_path_file);
					if (bimg_logo2 != null && outputfile != null) {
					ImageIO.write(bimg_logo2, "png", outputfile);
					}
					return true;

				} catch (IOException e) {
					logger.error("Erreur", e);
				}
			}
		}
		
		return false;
	}

	public static String getFormat(InputStream stream) throws IOException {
		ImageInputStream iis = ImageIO.createImageInputStream(stream);
		Iterator iter = ImageIO.getImageReaders(iis);
		if (!iter.hasNext()) {
			throw new IOException("Unsupported image format!");
		}
		ImageReader reader = (ImageReader) iter.next();
		iis.close();
		return reader.getFormatName();
	}

	public static String getMime(BufferedImage image) {
		String mime = null;
		return mime;
	}

	
	public static BufferedImage rotate90DX(BufferedImage img) {
		int         width  = img.getWidth();
		int         height = img.getHeight();
		BufferedImage   newImage = new BufferedImage( height, width, img.getType() );

		for( int i=0 ; i < width ; i++ )
			for( int j=0 ; j < height ; j++ )
				newImage.setRGB( height-1-j, i, img.getRGB(i,j) );

		return newImage;
	}

	public static BufferedImage rotate90ToLeft( BufferedImage inputImage ){
		//The most of code is same as before
		int width = inputImage.getWidth();
		int height = inputImage.getHeight();
		BufferedImage returnImage = new BufferedImage( height, width , inputImage.getType()  );
		//We have to change the width and height because when you rotate the image by 90 degree, the
		//width is height and height is width <img src='http://forum.codecall.net/public/style_emoticons/<#EMO_DIR#>/smile.png' class='bbc_emoticon' alt=':)' />

		for( int x = 0; x < width; x++ ) {
			for( int y = 0; y < height; y++ ) {
				returnImage.setRGB(y, width - x - 1, inputImage.getRGB( x, y  )  );
				//Again check the Picture for better understanding
			}
		}
		return returnImage;

	}

	public static BufferedImage rotate180( BufferedImage inputImage ) {
		//We use BufferedImage because it’s provide methods for pixel manipulation
		int width = inputImage.getWidth(); //the Width of the original image
		int height = inputImage.getHeight();//the Height of the original image

		BufferedImage returnImage = new BufferedImage( width, height, inputImage.getType()  );
		//we created new BufferedImage, which we will return in the end of the program
		//it set up it to the same width and height as in original image
		// inputImage.getType() return the type of image ( if it is in RBG, ARGB, etc. )

		for( int x = 0; x < width; x++ ) {
			for( int y = 0; y < height; y++ ) {
				returnImage.setRGB( width-x-1, height - y - 1, inputImage.getRGB( x, y  )  );
			}
		}
		//so we used two loops for getting information from the whole inputImage
		//then we use method setRGB by whitch we sort the pixel of the return image
		//the first two parametres is the X and Y location of the pixel in returnImage and the last one is the //source pixel on the inputImage
		//why we put width – x – 1 and height –y – 1 is hard to explain for me, but when you rotate image by //180degree the pixel with location [0, 0] will be in [ width, height ]. The -1 is for not to go out of
		//Array size ( remember you always start from 0 so the last index is lower by 1 in the width or height
		//I enclose Picture for better imagination  ... hope it help you
		return returnImage;
		//and the last return the rotated image

	}

	public static BufferedImage createRectangleTransparent(int width, int height) {
		BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		return bufferedImage;

	}

	/**
	 * Ajoute l'image buff2 par dessus l'image buff1 et retounre buff1
	 * @param buff1
	 * @param buff2
	 * @param x
	 * @param y
	 * @return
	 */
	public static BufferedImage addImage(BufferedImage buff1, BufferedImage buff2,
			int x, int y) {
		Graphics2D g2d = buff1.createGraphics();
		g2d.drawImage(buff2, x, y, null);
		g2d.dispose();
		g2d.drawImage(buff1, null, 0, 0);
		return buff1;
	}


	/**
	 * Redimensionne l'image en gardant les proportions
	 * @param srcImage
	 * @param pathImage
	 * @param targetWidth
	 * @param targetHeight
	 * @return retourne l'image redimensionnée ou null si il y a eu un problème
	 */
	public static BufferedImage resizeImage(BufferedImage srcImage, String pathImage ,int targetWidth, int targetHeight){  
		try {  
			double determineImageScale = determineImageScale(srcImage.getWidth(), srcImage.getHeight(), targetWidth, targetHeight);  
			BufferedImage dstImage = scaleImage(srcImage, determineImageScale);  
			ImageIO.write(dstImage, "jpg", new File(pathImage));  
			return dstImage;  
		} catch (IOException e) {  
			logger.error("IOException ImageUtils.resizeImage pathImage=" + pathImage + " targetWidth=" + targetWidth + " targetHeight=" + targetHeight + " erreur=", e);
			return null;
		}  
	}  
	private static BufferedImage scaleImage(BufferedImage sourceImage, double scaledWidth) {  
		Image scaledImage = sourceImage.getScaledInstance((int) (sourceImage.getWidth() * scaledWidth), (int) (sourceImage.getHeight() * scaledWidth), Image.SCALE_SMOOTH);  
		BufferedImage bufferedImage = new BufferedImage(  
				scaledImage.getWidth(null),  
				scaledImage.getHeight(null),  
				BufferedImage.TYPE_INT_RGB  
				);  
		Graphics g = bufferedImage.createGraphics();  
		g.drawImage(scaledImage, 0, 0, null);  
		g.dispose();  
		return bufferedImage;  
	}  
	public static double determineImageScale(int sourceWidth, int sourceHeight, int targetWidth, int targetHeight) {  
		double scalex = (double) targetWidth / sourceWidth;  
		double scaley = (double) targetHeight / sourceHeight;  
		return Math.min(scalex, scaley);  
	}  

	public static byte[] scale(byte[] fileData, int width, int height) {
	    ByteArrayInputStream in = new ByteArrayInputStream(fileData);
	    try {
	        BufferedImage img = ImageIO.read(in);
	        if(height == 0) {
	            height = (width * img.getHeight())/ img.getWidth(); 
	        }
	        if(width == 0) {
	            width = (height * img.getWidth())/ img.getHeight();
	        }
	        Image scaledImage = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
	        BufferedImage imageBuff = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	        imageBuff.getGraphics().drawImage(scaledImage, 0, 0, new Color(0,0,0), null);

	        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

	        ImageIO.write(imageBuff, "jpg", buffer);

	        return buffer.toByteArray();
	    } catch (IOException e) {
	        logger.error("IOException", e);
	    }
	    return null;
	}

	/**
	 * This method takes in an image as a byte array (currently supports GIF, JPG, PNG and
	 * possibly other formats) and
	 * resizes it to have a width no greater than the pMaxWidth parameter in pixels.
	 * It converts the image to a standard
	 * quality JPG and returns the byte array of that JPG image.
	 *
	 * @param pImageData
	 *                the image data.
	 * @param pMaxWidth
	 *                the max width in pixels, 0 means do not scale.
	 * @return the resized JPG image.
	 * @throws IOException
	 *                 if the image could not be manipulated correctly.
	 */
	/*public static byte[] resizeImageAsJPG2(byte[] pImageData, int pMaxWidth) throws IOException {
		InputStream imageInputStream = new ByteArrayInputStream(pImageData);
		// read in the original image from an input stream
		SeekableStream seekableImageStream = SeekableStream.wrapInputStream(imageInputStream, true);
		RenderedOp originalImage = JAI.create(JAI_STREAM_ACTION, seekableImageStream);
		((OpImage) originalImage.getRendering()).setTileCache(null);
		int origImageWidth = originalImage.getWidth();
		// now resize the image
		double scale1 = 1.0;
		double scale2 = 1.0;
		if (pMaxWidth > 0 && origImageWidth > pMaxWidth) {
			scale1 = (double) pMaxWidth / originalImage.getWidth();
			scale2 = (double) pMaxWidth / originalImage.getHeight();
		}
		ParameterBlock paramBlock = new ParameterBlock();
		paramBlock.addSource(originalImage); // The source image
		paramBlock.add(scale1); // The xScale
		paramBlock.add(scale2); // The yScale
		paramBlock.add(0.0); // The x translation
		paramBlock.add(0.0); // The y translation

		RenderingHints qualityHints = new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

		RenderedOp resizedImage = JAI.create(JAI_SUBSAMPLE_AVERAGE_ACTION, paramBlock, qualityHints);

		// lastly, write the newly-resized image to an output stream, in a specific encoding
		ByteArrayOutputStream encoderOutputStream = new ByteArrayOutputStream();
		JAI.create(JAI_ENCODE_ACTION, resizedImage, encoderOutputStream, JAI_ENCODE_FORMAT_JPEG, null);
		// Export to Byte Array
		byte[] resizedImageByteArray = encoderOutputStream.toByteArray();
		return resizedImageByteArray;
	}*/

	/**
	 * Redimensionne l'image passé en paramètre, au format jpg, en conservant la proportion
	 * @param file : le file avec chemin vers l'image à redimensionner
	 * @param width : la dimension
	 *//*
	public static byte[] resizeFromFile(File file, int width) {

		byte[] b = new byte[(int) file.length()];
		try {
			FileInputStream fileInputStream = new FileInputStream(file);
			fileInputStream.read(b);			
		} catch (FileNotFoundException e) {
			logger.error("File Not Found.", e);
		}
		catch (IOException e1){
			logger.error("Error Reading The File.", e1);
		}

		try {
			return resizeImageAsJPG2(b, width);
		}
		catch(FileNotFoundException ex) {
			logger.error("FileNotFoundException : ", ex);
		}
		catch(IOException ioe) {
			logger.error("IOException : ", ioe);
		}
		return null;

	}*/

	/**
	 * Ecrit le contenu du tableau de bytes strContent dans le fichier situé strFilePath
	 * @param strFilePath
	 * @param strContent
	 */
	public static void convertByteArrayToFile(String strFilePath, byte[] strContent) {

		try {
			FileOutputStream fos = new FileOutputStream(strFilePath);
			fos.write(strContent);
			fos.close(); 
		}
		catch(FileNotFoundException e) {
			logger.error("FileNotFoundException : ", e);
		}
		catch(IOException e) {
			logger.error("IOException : ", e);
		}

	}

	/**
	 * Ecrit un fichier dans un byte[]
	 * @param strFilePath
	 * @param strContent
	 */
	public static byte[] convertFileToByteArray(String strFilePath) {

		File file = new File(strFilePath);

		byte[] b = new byte[(int) file.length()];
		try {
			FileInputStream fileInputStream = new FileInputStream(file);
			fileInputStream.read(b);			
		} catch (FileNotFoundException e) {
			logger.error("File Not Found.", e);
			logger.error("error : ", e);
		}
		catch (IOException e1) {
			logger.error("Error Reading The File.", e1);
			logger.error("error : ", e1);
		}
		return b;

	}

	public static byte[] convertFileToByteArray(File strFilePath) {
		return convertFileToByteArray(strFilePath.getAbsolutePath());
	}

	/**
	 * Converti un byte[] en BufferedImage en gérant la transparence
	 * @param photo le  byte[] 
	 * @return
	 * @throws IOException
	 */
	public static BufferedImage bytesArrayToBufferedImageWithTransparent(byte[] photo) throws IOException {
		ByteArrayInputStream bais = new ByteArrayInputStream(photo);
		BufferedImage bufferedImage = ImageIO.read(bais);
		if (bufferedImage != null) {
			BufferedImage newBufferedImage = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
			newBufferedImage =   addImage(newBufferedImage, bufferedImage, 0, 0);
			return newBufferedImage;
		}
		return null;
	}

	/**
	 * Converti un byte[] en BufferedImage
	 * @param photo le  byte[] 
	 * @return
	 * @throws IOException
	 */
	public static BufferedImage bytesArrayToBufferedImage(byte[] photo) throws IOException {
		InputStream in = new ByteArrayInputStream(photo);
		return ImageIO.read(in);
	}

	/**
	 * Converti un BufferedImage en byte[]
	 * @param photo le  byte[] 
	 * @return
	 * @throws IOException
	 */
	public static byte[] bufferedImageToBytesArray(BufferedImage photo, String mime) throws IOException {
		String formatName = "jpg";
		if ("image/png".equals(mime)) {
			formatName = "png";
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			ImageIO.write( photo, formatName, baos );
			baos.flush();
		} catch (IOException e) {
			logger.error("IOException dans bufferedImageToBytesArray", e );
		}
		return baos.toByteArray();
	}

	/**
	 * Redimensionne l'image passé en paramètre en utilisant les nouvelles dimensions passées en paramètre
	 * @param img l'image
	 * @param newW la largeur
	 * @param newH la hauteur
	 * @return l'image redimensionnée
	 */
	public static BufferedImage resize(BufferedImage img, int newW, int newH) {  
		int w = img.getWidth();  
		int h = img.getHeight();  
		BufferedImage dimg = new BufferedImage(newW, newH, img.getType());  
		Graphics2D g = dimg.createGraphics();  
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);  
		g.drawImage(img, 0, 0, newW, newH, 0, 0, w, h, null);  
		g.dispose();  
		return dimg;  
	}  

	

	/**
	 * Clone un BufferedImage
	 * @param bi
	 * @return
	 */
	public static BufferedImage cloneBufferedImage(BufferedImage bi) {
		ColorModel cm = bi.getColorModel();
		boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
		WritableRaster raster = bi.copyData(null);
		return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
	}

	/**
	 * Calcule le pourcentage de zone grise de l'image passée en paramètre
	 * Une zone grise indique une coupure réseau pendant le transfère de l'image
	 * @param file
	 * @return
	 */
	public static double calculeGreyZone(File file) {
		double percent = 0;
		BufferedImage image = null;
		try {
			image = ImageIO.read(file);
		} catch (IOException e) {
			logger.error("Image null pour le calcul de pourcentage de zone grise", e);
		}
		if (image != null) {

			long nb_pixels_gris = 0; // hexa :#808080 // rgb : 128 128 128 // -8355712
			long nb_pixels = image.getWidth() * image.getHeight();

			for (int x = 0; x < image.getWidth(); x++) {
				for (int y = 0; y < image.getHeight(); y++) {
					if (new Color(image.getRGB(x, y)).getRGB() == -8355712) {
						nb_pixels_gris++;
					}
				}
			}

			percent = nb_pixels_gris * 100 / nb_pixels;
		}
		return percent;
	}
}
