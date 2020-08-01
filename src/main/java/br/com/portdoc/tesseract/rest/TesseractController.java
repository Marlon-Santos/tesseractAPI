package br.com.portdoc.tesseract.rest;


import net.sourceforge.tess4j.Tesseract;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.IOException;

@RestController
@RequestMapping("api/v1/")
public class TesseractController {
    @PostMapping("upload")
    public String retornoOCR(@RequestParam("file") MultipartFile file) throws IOException {
        BufferedImage image = ImageIO.read(file.getInputStream());
        return retornarTextoOCR(image);
    }

    public String retornarTextoOCR(BufferedImage ipimage) throws IOException {
        String strRetorno = "";
        try {
            double d = ipimage.getRGB(ipimage.getWidth() / 2, ipimage.getHeight() / 2);

            // Comparando os valores e setando as novas escalas de valores para ser usado no RescaleOP
            if (d >= -1.4211511E7 && d < -7254228) {
                strRetorno = processImg(ipimage, 3f, -10f);
            } else if (d >= -7254228 && d < -2171170) {
                strRetorno = processImg(ipimage, 1.455f, -47f);
            } else if (d >= -2171170 && d < -1907998) {
                strRetorno = processImg(ipimage, 1.35f, -10f);
            } else if (d >= -1907998 && d < -257) {
                strRetorno = processImg(ipimage, 1.19f, 0.5f);
            } else if (d >= -257 && d < -1) {
                strRetorno = processImg(ipimage, 1f, 0.5f);
            } else if (d >= -1 && d < 2) {
                strRetorno = processImg(ipimage, 1f, 0.35f);
            } else {
                strRetorno = processImg(ipimage, 0f, 0f);
            }
        } catch (Exception e) {
            strRetorno = "Não foi Possivel scanear o texto da imagem. Erro durante conversão." + e.getMessage();
        }
        return strRetorno;
    }


    public String processImg(BufferedImage ipimage, float scaleFactor, float offset) {
        String strRetorno = "";
        BufferedImage fopimage = null;
        try {
            if (scaleFactor != 0f && offset != 0f) {
                BufferedImage opimage = new BufferedImage(1050, 1024, ipimage.getType());
                Graphics2D graphic = opimage.createGraphics();
                graphic.drawImage(ipimage, 0, 0, 1050, 1024, null);
                graphic.dispose();
                RescaleOp rescale = new RescaleOp(scaleFactor, offset, null);
                fopimage = rescale.filter(opimage, null);
            } else {
                fopimage = ipimage;
            }
            Tesseract instance = new Tesseract();
            instance.setDatapath("src/main/resources");
            instance.setLanguage("por");
            strRetorno = instance.doOCR(fopimage);
        } catch (Exception e) {
            strRetorno = e.getMessage();
        }
        return strRetorno;
    }
}
