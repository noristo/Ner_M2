package test_install_coreNLP;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


import org.apache.commons.lang3.StringUtils;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.ling.CoreAnnotations;



public class principale {

    public static void main(String[] args) throws IOException { //throw nous permet d'ouvrir le fichier csv, on n'a pas défini de catch
    	// création d'un objet Properties
    	Properties props = new Properties();
    	// définition du pipeline
    	// Propriétés du pipeline: Le Tokenizeur, découpeur et POS Tagger sont des annotateurs par défaut
    	// On ajoute un lemmatiseur et un NER model externe
    	props.setProperty("annotators", " tokenize,ssplit,pos,custom.lemma,ner");
    	
    	
    	// paramétrage pour le français
    	props.setProperty("props", "StanfordCoreNLP-french.properties");
    	props.setProperty("tokenize.language","French"); props.setProperty("parse.model","edu/stanford/nlp/models/lexparser/frenchFactored.ser.gz");
    	props.setProperty("pos.model","edu/stanford/nlp/models/pos-tagger/french/french.tagger");
    	props.setProperty("tokenize.verbose","false"); // True = affiche les tokens
    	props.setProperty("ner.useSUTime", "false");
    	
    	// paramétrage du tokenizer
    	props.setProperty("tokenize.options", "untokenizable=noneDelete"); // supprime les séquences qui ne peuvent être tokenzié (https://stanfordnlp.github.io/CoreNLP/tokenize.html)
		
    	// paramétrage du sentence splitter (https://stanfordnlp.github.io/CoreNLP/ssplit.html)
    	props.setProperty("ssplit.newlineIsSentenceBreak", "always"); // Prend un \n comme fin de phrase
    	
    	// Ajout du custom lemmatiseur et du Path du fichier lexique (Remarque: on n'as pas ajouté de path Relatif car cela cause des erreurs)
    	// lexique_fr.txt est extrait du logiciel iramuteq http://iramuteq.org/telechargement 
    	// le dictionnaire ABU n'est apparemment pas adapté, même s'ils sont tout les deux sous le même format et contiennent les mêmes informations
    	props.setProperty("customAnnotatorClass.custom.lemma", "test_install_coreNLP.CustomLemmatiseur");
    	props.setProperty("french.lemma.lemmaFile", "C:/Users/Yazid/Desktop/abu/lexique_fr.txt");
    	
  
    	// Ajout du custom NER Mode
    	// Le  modèle europeana est téléchargé d'ici :  http://lab.kbresearch.nl/static/html/eunews.html
    	props.setProperty("ner.model", "C:/Users/Yazid/Desktop/abu/ner-model.ser.gz");
    	
    	// création du pipeline
    	StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
    	
    	//création du fichier de sortie
    	PrintWriter writer = new PrintWriter(new File("C:/Users/Yazid/Desktop/abu/test_result.csv"));
    	StringBuilder sb = new StringBuilder();
    	sb.append("Ligne"+";"+"Mots retenus"+";"+ "PoS"+";"+"lemmes"+";"+"Ner_PERS"+";"+"Ner_ORG"+";"+"Ner_LIEU"+";"+"Ner_COVID"+"\n"); //il arrive que parfois les cases soient décalées à gauche dans le fichier de sortie 
    	writer.write(sb.toString());
    	
    	// Lance le pipeline sur le corpus 
    	String path = "C:/Users/Yazid/Desktop/corpus/val/evalu.csv";
    	String ligne="";
    	List<String> lines = new ArrayList<>();
    	BufferedReader br = new BufferedReader(new FileReader(path));
    	while((ligne = br.readLine()) != null){
    		lines.add(ligne);
    
    	// créer une annotation vide avec le texte
    	Annotation document = new Annotation(ligne);
    	
    	// lance l'annotation sur le texte
    	pipeline.annotate(document);
    	
    	// déclarations de listes contenant les résultats
    	ArrayList<String> results_words = new ArrayList<>(); 
    	ArrayList<String> results_pos = new ArrayList<>(); 
    	ArrayList<String> results_person = new ArrayList<>(); 
    	ArrayList<String> results_organization = new ArrayList<>(); 
    	ArrayList<String> results_location = new ArrayList<>(); 
    	ArrayList<String> results_lemma = new ArrayList<>(); 
    	ArrayList<String> results_COVID = new ArrayList<>();
    	
    	
    	// Obtenir la liste des phrases 
    	List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
    	for (CoreMap sentence : sentences) {
    		
    		// traitement des tokens
    		for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
    		//On réduit en minuscule les tokens
            String word = token.get(TextAnnotation.class).toLowerCase();
            String lemma ="";
            //S'il s'agit bien d'un mot,
            if ((StringUtils.containsAny(word,"\\.?,;!*)(&#}{][-_@/")==false) && word.length()>1){
                // on récupère son type (PoS)
               String pos = token.get(PartOfSpeechAnnotation.class);
               // tous les types de mots ne sont pas des entités nommées. 
               // On ne conserve que les noms, verbes, adjectifs. On filtre les déterminants, les prépositions, etc...
               if (!(pos.equals("PUNC") || pos.equals("DET") || pos.equals("DET") || pos.equals("P") || pos.equals("CC") || pos.equals("PROREL")|| 
            		   pos.equals("PRO")|| pos.equals("CLS")|| pos.equals("ADV") || pos.equals("CS") || pos.equals("PROWH")|| pos.equals("CLO")|| pos.equals("CL")|| pos.equals("CLR")
            		   || pos.equals("C")|| lemma.equals("être")|| lemma.equals("avoir")))
               {
            	// On récupère le lemme 
            	   lemma = token.get(LemmaAnnotation.class);
                   lemma=lemma.toLowerCase();
               
               // stockage du mot,son annotation POS et sa forme lemmatisée
               results_words.add(word);
               results_pos.add(pos);
               results_lemma.add(lemma);
               // On récupère les entités nommées (entreprise, personne...)
               String ner = token.get(NamedEntityTagAnnotation.class);
                // s'il s'agit bien d'une entité nommée, on stocke le résultat dans la bonne variable
               if (!(ner.equals("O")) ) {
                   // une personne
                   if(ner.equals("PERS")) {
                        results_person.add(word);
                   }
                   // une organisation
                   else if (ner.equals("ORG")) {
                      results_organization.add(word);
                   } 
                   // un lieu
                   else if (ner.equals("LIEU")) {
                      results_location.add(word);
                   } 
                   // COVID
                   else {
                      results_COVID.add(word);
                   }
               }
           }
        }
    	}
    	}
    	//on écrit les resultat de cette ligne dans le fichier
    	StringBuilder sb1 = new StringBuilder();
    	String sortie = ligne+";"+results_words+";"+ results_pos+";"+results_lemma+";"+results_person+";"+results_organization+";"+results_location+";"+results_COVID+"\n";
    	sb1.append(sortie);
    	writer.write(sb1.toString());
    }
   }
}	
