package negocio;
/**
 *
 * @author Gabriel Roda
 * @author Matias Henschel
 * @author Nicolas Frasson
 * 
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Logger;

public class SRTChecker {

    private int writeMethod;
    FileReader readF;
    ArrayList<String> errors = new ArrayList<String>();

    public void setFile(File path) throws Exception {
        String extension = path.toString().substring(path.toString().lastIndexOf(".") + 1);
        if (!extension.equals("srt")) {
            throw new Exception("Tipo de arquivo inválido. Deve ser um arquivo '.srt'.");
        }
        readF = new FileReader(path);
    }

    public void check() throws Exception {
        BufferedReader reader = new BufferedReader(readF);
        errors.clear();
        String line;
        int step = 0, lineCounter = 1;
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss,SSS");
        Date start = null, end = null;
        try {
            line = reader.readLine();
            while (line != null) {
                line = line.trim();
                switch (step) {
                    case 0: // Procura por um número inteiro (numeração da legenda)
                        if (line.length() > 0) {
                            try {
                                Integer.parseInt(line);

                                step++; // Avança para a próxima verificação
                            } catch (Exception e) {
                                errors.add("Linha " + lineCounter + ": não contém um contador inteiro.");
                            }
                        }
                        break;
                    case 1: // Procura pelos tempos da legenda e faz verificações
                        line = line.toLowerCase();
                        try {
                            int arrowPos = line.indexOf("-->"), x1Pos = line.indexOf("x1");

                            if (arrowPos < 0) {
                                throw new Exception("'-->' não encontrado.");
                            }

                            String first = line.substring(0, arrowPos).trim(),
                                    second;
                            if (x1Pos < 0) {
                                second = line.substring(arrowPos + 3).trim();
                            } else {
                                second = line.substring(arrowPos + 3, x1Pos).trim();
                            }

                            step++; // Avança pra próxima verificação mesmo que formato da data esteja errado

                            start = sdf.parse(first);
                            if (end != null) {
                                if (end.after(start)) {
                                    throw new Exception("legenda inicia antes do término da anterior.");
                                }
                            }
                            end = sdf.parse(second);
                            if (end.before(start)) {
                                throw new Exception("legenda termina antes de iniciar.");
                            }
                        } catch (Exception e) {
                            errors.add("Linha " + lineCounter + ": " + e.getMessage());
                        }
                        break;
                    case 2: // Procura pelo texto da legenda
                        try {
                            if (line.length() == 0) {
                                throw new Exception();
                            }
                            step++;
                        } catch (Exception e) {
                            errors.add("Linha " + lineCounter + ": legenda em branco.");
                            step = 0;
                        }
                        break;
                    case 3: // Procura por um espaço em branco separando legendas
                        if (line.length() == 0) {
                            step = 0;
                        }
                        break;
                }
                line = reader.readLine();
                lineCounter++;
            }
        } finally {
            reader.close();
        }
        writeErrors();
    }

    public void setWriteMethod(int method) throws Exception {
        if (method < 0 || method > 2) {
            throw new Exception("Favor selecionar um método de saída.");
        } else {
            this.writeMethod = method;
        }
    }

    private void writeTextFile() throws FileNotFoundException, IOException {
        File file = new File("Erros.txt");
        FileOutputStream data = new FileOutputStream(file);
        for (String st : errors) {
            String text = st + "\n";
            data.write(text.getBytes());
        }
    }

    public void writeErrors() throws Exception {
        switch (writeMethod) {
            case 0:
                for (String st : errors) {
                    System.out.println(st);
                }
                break;
            case 1:
                writeTextFile();
                break;
            case 2:
                Logger logger;
                for (String st : errors) {
                    logger = Logger.getLogger(st);
                    logger.warning(st);
                }
                break;
            default:
                throw new Exception("Método inexistente!");
        }
    }
}
