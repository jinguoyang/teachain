package com.youzi.teaChain.common;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Hashtable;

public class QrCodeUtils {

    public static String createQrCode(String name, String path, String contents, int width, int height) throws Exception {
        Hashtable<EncodeHintType, java.io.Serializable> hints = new Hashtable<>();
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
//        hints.put(EncodeHintType.MAX_SIZE, 350);//设置图片的最大值
//        hints.put(EncodeHintType.MIN_SIZE, 200);//设置图片的最小值
        hints.put(EncodeHintType.MARGIN, 2);   //设置白边
        try {
            BitMatrix bitMatrix = new MultiFormatWriter().encode(contents, BarcodeFormat.QR_CODE, width, height, hints);

            //直接写入文件
            //创建文件
            File dir=new File(path);
            if(!dir.exists()){
                dir.mkdirs();
            }
            Path outputFilePath = Paths.get(path + "/" + name + ".jpg");
            MatrixToImageWriter.writeToPath(bitMatrix, "jpg", outputFilePath);
        } catch (WriterException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
        return name;
    }

    /**
     * image流数据处理
     *
     * @author ianly
     */
    public static BufferedImage toBufferedImage(BitMatrix matrix) {
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, matrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
            }
        }
        return image;
    }
}
