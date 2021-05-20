/*
 * Projeto feito para a matéria de IA na faculdade
 * Membros do grupo:
 * Richard Fernandes
 * Gustavo Alves
 * Henrique
 * Giovaninni Barbosa
 */
//java -jar Agente.jar
package teste;

import java.util.ArrayList;
import java.util.List;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
//import org.opencv.highgui.HighGui;
import org.opencv.imgproc.Imgproc;
import java.io.File;

public class Main 
{

	public static void main(String[] args)
	{
		//System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		System.load(System.getProperty("user.dir")+"/Agente_lib/libopencv_java452.so"); //carregando biblioteca
		
		//lendo argumentos recebidos
		int sensibilidade=1;
		int caixa=2;
		if (args.length > 1)
		{
			sensibilidade=Integer.parseInt(args[1]);				
			caixa=Integer.parseInt(args[2]);
		}
		
		//criando diretório destino
		File caminho=new File(System.getProperty("user.dir")+"/resultado");
		if (!caminho.exists())
		{
			if (caminho.mkdir())
				System.out.println("\t----Diretório resultado criado");
			else
			{
				System.out.println("\t----Erro ao criar diretório");
				return;
			}
		}
		//preparações para carregar múltiplas imagens
		int ind = Integer.parseInt(args[0]);
		int nofile=1;
		Integer index,ofile=1;
		String aux;		
		//usadas para findcountours
		List<MatOfPoint> contours;
		Mat hierarchy;     
		//Auxiliares
		Mat source;
		Mat destination;
		Mat gray = new Mat();
		Mat mask = new Mat(3, 3, CvType.CV_8UC1);
		Mat morphOutput = new Mat();
		Mat erodeElement;
		if (caixa==2)
			erodeElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(12, 12));
		else
			erodeElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(caixa*5, caixa*5));
		Mat dilateElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(14, 14));
		Rect rect;
        Rect rectCrop;
        Mat test, fim;
        ArrayList<Mat> arr;//conterá as imagens finais (cortadas e filtradas)
        int flag=0;//flag usada para filtragem de imagens repetidas
    
		//Variaveis de valores HSV da imagem
        Scalar minValues;
        if (sensibilidade==1)
        	minValues = new Scalar(1,1,250);
        else
        	minValues = new Scalar(sensibilidade*5,sensibilidade*5,250);
		Scalar maxValues = new Scalar(245,245,250);
    		
		for (int i=0;i<ind;i++)
		{			
			contours = new ArrayList<>();
			arr = new ArrayList<Mat>();
			hierarchy = new Mat();
			index=i;
			aux=System.getProperty("user.dir")+"/"+index.toString()+".png";
			source = Imgcodecs.imread(aux); //carregando imagem do diretório
            
            destination = new Mat(source.rows(), source.cols(), source.type());
            
			Imgproc.cvtColor(source, gray, Imgproc.COLOR_RGB2GRAY); //Convertendo pra tons de cinza
        	Core.inRange(gray, minValues, maxValues, mask);          //Criando uma mascara de imagem preta e branca de acordo com os intervalos HSV
        	
        	//operações morfologicas
			Imgproc.erode(mask, morphOutput, erodeElement);         //expande
			Imgproc.dilate(morphOutput, destination, dilateElement);//contrai

			Imgproc.equalizeHist(destination, destination); //equalizando e consertando bordas da máscara
			Imgproc.GaussianBlur(destination, destination, new Size(5, 5), 0, 0, Core.BORDER_DEFAULT);
			Imgproc.Canny(destination, destination, 100, 300);
			Imgproc.threshold(destination, destination, 0, 255, Imgproc.THRESH_BINARY);
			
			//Encontrando bordas
			Imgproc.findContours(destination, contours, hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);
			
			//fazendo corte
			fim=new Mat();
	        for (int a=0;a<contours.size();a++) 
	        {        
	            Mat contour = contours.get(a);
	            double contourArea= Imgproc.contourArea(contour);
	            if(contourArea>450) //apenas considera imagens maiores que um certo limite
	            {
	            	flag=0;
	                rect= Imgproc.boundingRect(contours.get(a));
	                rectCrop= new Rect(rect.x,rect.y,rect.width,rect.height);
	                test = new Mat(source,rectCrop);//variavel test contém a imagem cortada
	                if (arr.size()==0)
	                	arr.add(test);
	                else 
	                {
	                	for (int corr=0; corr < arr.size();corr++)//testando se é repetida
	                	{
	                		if(test.width() == arr.get(corr).width() && test.height() == arr.get(corr).height())
	                		{
	                			Imgproc.matchTemplate(arr.get(corr), test, fim, Imgproc.TM_CCORR_NORMED);//método de comparação de imagens
	                			Core.MinMaxLocResult minMaxLocRes = Core.minMaxLoc(fim); //a matriz fim contém as chances de cada pixel ser o centro da imagem teste, depois pega o maior valor dessa matriz
	                			if (minMaxLocRes.maxVal>0.99) //caso a chance de a imagem testada ser/estar contida na primeira imagem seja maior do que 99%, a ignora 
	                			{
	                				flag=1;
	                				break;
	                			}
	                		}
	                	}
	                	if (flag==0) //apenas caso não seja repetida, adiciona ao array
	                		arr.add(test);
	                }
	            }   
	        }
            caminho=new File (System.getProperty("user.dir")+"/resultado/"+ofile.toString()); //criando pastas para cada pagina
            while (true)
            {
            	if (caminho.exists())
            	{
            		nofile++;
            		ofile=nofile;
            		caminho=new File (System.getProperty("user.dir")+"/resultado/"+ofile.toString());
            	}
            	else
            	{
            		if (caminho.mkdir())
        				System.out.println("\t-Diretório para página número "+ofile.toString()+" criado");
        			else
        			{
        				System.out.println("\t-Erro ao criar diretório para página");
        				return;
        			}
            		break;
            	}
            }
            for (int corr=0; corr<arr.size();corr++)
            {
            	Imgcodecs.imwrite(caminho.toString()+"/"+String.valueOf(corr)+".png", arr.get(corr));
            }
		}
	}
}