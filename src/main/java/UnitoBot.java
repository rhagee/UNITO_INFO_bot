import com.google.inject.internal.Messages;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.groupadministration.DeleteChatStickerSet;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import java.io.*;
import java.sql.Driver;
import java.util.ArrayList;
import java.util.List;

public class UnitoBot extends TelegramLongPollingBot
{
    Message oldMessage = new Message();

    //UPDATE
    public void onUpdateReceived(Update update) {
        try
        {
            oldMessage=update.getMessage();
            SendMessage message = new SendMessage(); //Messaggio a fine Update
            if(update.hasCallbackQuery()) //When
            {
                //Raccolta dati dal Return
                String data = update.getCallbackQuery().getData();
                String type = update.getCallbackQuery().getMessage().getText();
                Chat currentchat = update.getCallbackQuery().getMessage().getChat();

                //Delete dei Tasti
                Delete(update.getCallbackQuery().getMessage().getChatId(),update.getCallbackQuery().getMessage().getMessageId());

                //Switch Case del tipo di Return Ottenuto
                if(type.contains("Dipartimento"))
                {
                    StoreDip(currentchat,data);
                    SendMessage dipmessage = new SendMessage();
                    dipmessage.setText("Il tuo <b>Dipartimento</b> è stato memorizzato correttamente, prosegui inserendo il tuo Corso di Laurea attraverso il comando <i>/setcourse</i> ! \n PS.Ricordati che puoi cambiare il dipartimento in ogni momento, riutilizzando il comando <i>/setdip</i>.");
                    dipmessage.setChatId(update.getCallbackQuery().getMessage().getChatId());
                    dipmessage.setParseMode("html");
                    execute(dipmessage);
                }
                else if(type.contains("Corso"))
                {
                    StoreCourse(currentchat,data);
                    SendMessage courseMessage = new SendMessage();
                    courseMessage.setText("Il tuo <b>Corso di Laurea</b> è stato memorizzato correttamente, quando vorrai potrai utilizzare il comando <i>/find</i> per trovare la materia di cui vuoi conoscere i prossimi appelli! \n PS.Ricordati che puoi cambiare il corso di laurea in ogni momento, riutilizzando il comando <i>/setcourse</i>.");
                    courseMessage.setChatId(update.getCallbackQuery().getMessage().getChatId());
                    courseMessage.setParseMode("html");
                    execute(courseMessage);
                }
                else if(type.contains("attività"))
                {

                    PrintTab(currentchat,data);
                }
            }
            else //Nel caso in cui fosse un comando da Tastiera
            {

                String command = update.getMessage().getText();
                //Switch case del tipo di comando
                if(command.equals("/start"))
                {
                    message.setText("Ciao, sono il BOT per la visualizzazione degli Appelli UNITO. Sono in versione ALPHA, verrò implementato nel tempo sopratutto in base alle richieste dell'utenza. Puoi iniziare impostando il Dipartimento di questa chat! Usa il comando /setdip. \nInoltre potrai dare consigli , o fare richieste agli sviluppatori tramite il comando /alpha \n\n Se non mi hai mai utilizzato prima , posso spiegarti il mio funzionamento tramite il comando /help.");
                    message.setChatId(update.getMessage().getChatId());
                    execute(message);
                }
                if ("/setdip".equals(command)) //Register
                {
                    Chat currentchat = update.getMessage().getChat();
                    printKeyboard(update,"dip");
                }
                else if ("/setcourse".equals(command)) //Informazioni
                {
                    Chat currentchat = update.getMessage().getChat();
                    printKeyboard(update,"course"); //Printing Keyboard
                }
                else if ("/find".equals(command))//Help
                {
                    Chat currentchat = update.getMessage().getChat();
                    printKeyboard(update,"activity"); //Printing Keyboard
                }
                else if(command.equals("/alpha")) //Chiamata per aggiornamento file ALPHA.txt
                {
                    oldMessage = update.getMessage();
                    message.setText("Grazie per aver utilizzato la funzione ALPHA, perfavore scrivi il messaggio che vorresti venisse letto dagli Sviluppatori, se opportuno provvederanno ad aggiornarmi secondo le tue esigenze!");
                    message.setChatId(update.getMessage().getChatId());
                    execute(message);
                }
                else if("/help".equals(command))
                {
                    message.setText("Bene ti spiegherò velocemente il mio funzionamento : \n /setdip : Attraverso questo comando potrai farmi memorizzare il tuo Dipartimento, NON DOVRAI ri inserirlo ogni volta perchè lo terrò in memoria! Se vuoi modificarlo, puoi riutilizzare lo stesso comando, e selezionare un nuovo dipartimento " +
                            "\n /setcourse : Attraverso questo comando visualizzerai la lsita di tutti i corsi disponibili, una volta scelto uno, lo terrò in memoria, così non dovrai riselezionarlo ogni volta. Per cambiare il corso , potrai usare di nuovo il comando. " +
                            "\n /find : Attraverso questo comando potrai scegliere l'attività di cui vuoi trovare gli appelli, li visualizzerai non appena scelta. Ti saranno mostrati TUTTI gli appelli di cui UNITO fornisce informazioni" +
                            "\n /alpha : Attraverso questo comando potrai inviare un messaggio agli sviluppatori, potrai scrivere riguardo un problema riscontrato, o dare consigli per lo sviluppo di una mia prossima versione!" +
                            "\n PS. Se selezionerai il tuo dipartimento ed il tuo corso, e andrai a modificare il Dipartimento , anche il corso verrà CANCELLATO , quindi dovrai settare un nuovo corso del dipartimento scelto!" +
                            "\n\n\n SPERO ORA SIA TUTTO PIU CHIARO! Attendo un Comando!");
                    message.setChatId(update.getMessage().getChatId());
                    execute(message);
                }
                else if(oldMessage.getText().equals("/alpha")) //Aggiornamento file ALPHA.txt
                {
                    BufferedReader reader = new BufferedReader(new FileReader("alpha.txt")); //Reader File Chat
                    BufferedWriter writer = new BufferedWriter(new FileWriter("newalpha.txt")); //Writer File Chat
                    String line=null;
                    String tobe="";
                    while((line=reader.readLine())!=null)
                    {
                    tobe=tobe+line+"\n";
                    }
                    tobe=tobe+update.getMessage().getFrom().getFirstName()+" ha detto : "+update.getMessage().getText();
                    writer.write(tobe);
                    reader.close();
                    writer.close();
                    File f1 = new File("alpha.txt");
                    f1.delete();
                    File f2 = new File("newalpha.txt");
                    f2.renameTo(new File("alpha.txt"));
                    message.setText("Grazie per aver contribuito al mio miglioramento!\nOra puoi continuare ad utilizzare i comandi, gli sviluppatori leggeranno il primo possibile il tuo consiglio!");
                    message.setChatId(update.getMessage().getChatId());
                    execute(message);
                }
                oldMessage=update.getMessage();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //UPDATE

    //DELETE MESSAGE
    public void Delete(Long chatid, Integer messageid) throws Exception
    {
        DeleteMessage delete =  new DeleteMessage();
        delete.setChatId(chatid);
        delete.setMessageId(messageid);
        delete.validate();
        execute(delete);
    }

    //STORE DIP IN CHAT.TXT
    private void StoreDip(Chat currentchat,String data) throws Exception
    {
        //Reader e Writer
        BufferedReader reader = new BufferedReader(new FileReader("Chat.txt")); //Reader File Chat
        BufferedWriter writer = new BufferedWriter(new FileWriter("newChat.txt")); //Writer File Chat

        //String e boolean di appoggio
        String line = null;
        String newText="";
        boolean find = false;

        //Ciclo tutto il file
        while((line = reader.readLine())!=null)
        {
            if(line.contains("Chat"))
            {
                String[] chat = line.split(":");
                newText=newText+line+"\n";
                if(chat[1].equals(Long.toString(currentchat.getId())))
                {
                    line=reader.readLine(); //Prossima linea è DIP
                    newText=newText+"Dip:"+data+":\n"; //La sostituisco con questa
                    find = true; //Ho trovato DIP
                   line=reader.readLine(); //Controllo anche la prossima linea che può essere Corso
                   if(line==null || !(line.contains("course")) )
                   {
                       newText=newText+line+"\n"; //Se non è un corso allora la trascrivo
                   }
                }
            }
            else
            {
                newText = newText+line+"\n";
            }
        }
        if(!find) //Se non trovo il dipartimento allora creo un nuovo Record
        {
            newText=newText+"Chat:"+currentchat.getId()+":\n";
            newText=newText+"Dip:"+data+":\n";
        }
        //Copia e sostituzione file
        writer.write(newText);
        reader.close();
        writer.close();
        File f1 = new File("Chat.txt");
        f1.delete();
        File f2 = new File("newChat.txt");
        f2.renameTo(new File("Chat.txt"));
    }


    //STORE COURSE IN CHAT.txt
    private void StoreCourse(Chat currentchat,String data) throws Exception {
        //Scrittura
        BufferedReader reader = new BufferedReader(new FileReader("Chat.txt")); //Reader File Chat
        BufferedWriter writer = new BufferedWriter(new FileWriter("newChat.txt")); //Writer File Chat
        //Variabili di appoggio
        String line = null;
        String newText = "";
        boolean find = false;
        //Ciclo tutto il File
        while ((line = reader.readLine()) != null)
        {
            if (line.contains("Chat"))
            {
                String[] chat = line.split(":");
                newText = newText + line + "\n";
                if (chat[1].equals(Long.toString(currentchat.getId())))
                {
                    line = reader.readLine();//Salto la riga del dipartimento
                    newText = newText + line + "\n"; //copiandola
                    line=reader.readLine(); //Vado alla riga del corso da sostituire con una nuova
                    newText = newText + "course:" + data + ":\n";
                    if(line!=null&&!(line.contains("course"))) //Se la prossima linea non è NULLA e NON CONTIENE COURSE
                    {
                        newText=newText+line+"\n"; //La copio
                    } //Altrimenti conteneva il course che non va piu' copiato
                    find = true; //Trovato
                }
            }
            else //Se la parola non contiene CHAT la copio soltanto senza andare a fare il TEST
                {
                    newText = newText + line + "\n";
                }
        }
        //Se non ho trovato neanche una CHAT corrispondente, vuol dire che non è mai stato inserito un dipartimento
        if (!find)
        {
            //Messaggio di Errore
            SendMessage error = new SendMessage().setText("ERRORE DI SISTEMA : Prima di inserire un corso è necessario selezionare un dipartimento!").setChatId(currentchat.getId());
            execute(error);
        }

            //Sostituzione File
                writer.write(newText);
                reader.close();
                writer.close();
                File f1 = new File("Chat.txt");
                f1.delete();
                File f2 = new File("newChat.txt");
                f2.renameTo(f1);
        }



    //PRINT KEYBOARD
    public synchronized void printKeyboard(Update update,String Type) throws Exception
    {
        List<InlineKeyboardButton> Menu = InlineMenu(update,Type);
        //Inline Buttons
        InlineKeyboardMarkup replyKeyboardMarkup = new InlineKeyboardMarkup(); //Creo InlineKeyboard
        int numrows = 24; //Numero di righe
        if(Menu!=null)
        {
            replyKeyboardMarkup.setKeyboard(InlineK(Menu, numrows)); //Riempio la Keyboard
            //Impostazioni SendMessage
            SendMessage sendmessage = new SendMessage(); //Imposto il messaggio da mandare
            sendmessage.setChatId(update.getMessage().getChatId()); //Imposto il Chat ID
            if (Type.equals("dip"))
                sendmessage.setText("Selezione il tuo Dipartimento, questo verrà salvato per questa chat. \nPer modificarlo basterà usare il comando /setdip una ulteriore volta."); //Message
            else if (Type.equals("course"))
                sendmessage.setText("Selezione il tuo Corso di Laurea, questo verrà salvato per questa chat.\nPer modificarlo basterà usare il comando /setcourse una ulteriore volta.\n ATTENZIONE : Alcuni dipartimenti hanno piu corsi di laurea con lo stesso nome, provare ad utilizzare /find per trovare le proprie attività dopo aver selezionato il Corso."); //Message
            else if (Type.equals("activity"))
                sendmessage.setText("Selezione l'attività didattica di cui visualizzare i prossimi Appelli.\nSe non trovi la tua attività didattica puoi provare a cambiare il corso di Laurea!"); //Message
            else
                sendmessage.setText("ERROR: Called Function with an un-existing Type"); //Message

            sendmessage.setReplyMarkup(replyKeyboardMarkup); //Keyboard to Message
            //InvioMessaggio -> Print Keyboard
            execute(sendmessage); //Execute
        }
    }

    //PRENDI CORSO da CHAT.txt
    public String GetCourse(String ChatId) throws Exception
    {
        //corso inizia = null
        String course=null;
        BufferedReader reader = new BufferedReader(new FileReader("Chat.txt")); //Reader File UNITO
        String line = null;
        boolean find=false;
        //Cerco fino a quando non lo trovo o termino il file
        while((line=reader.readLine())!=null && !find)
        {
            if(line.contains(ChatId)) //Se la linea contiene il chat id che cerco
            {
                //Allora salto 2 righe e vado a prendere il VALUE del Corso
                line=reader.readLine();
                line = reader.readLine();
                if(line!=null)
                {
                    String[] temp = line.split(":");
                    course = temp[1];
                }
                find = true;
            }
        }
        reader.close();
        return course;
    }

    //GET DIP FROM CHAT.TXT -> uguale a quello sopra ma salto 1 riga solo per prendere il value di DIP
    public String GetDip(String ChatId) throws Exception
    {
        String dip=null;
        BufferedReader reader = new BufferedReader(new FileReader("Chat.txt")); //Reader File UNITO
        String line = null;
        boolean find=false;
        while((line=reader.readLine())!=null && !find)
        {
            if(line.contains(ChatId))
            {
                line=reader.readLine();
               String[] temp=line.split(":");
               dip = temp[1];
               find = true;
            }
        }
        reader.close();
        return dip;
    }

    // MENU CREATE
    public List<InlineKeyboardButton> InlineMenu(Update update,String MenuType) throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader("UNITO.txt")); //Reader File UNITO
        SendMessage error = new SendMessage();
        List<Option> GetMenu = new ArrayList();
        String ChatId = Long.toString(update.getMessage().getChatId());
        String dip = null;
        String course = null;
        if(MenuType.equals("dip"))
            GetMenu=findDip(reader);
        else if(MenuType.equals("course"))
        {
            dip = GetDip(ChatId);
            if(dip==null)
            {
                error.setChatId(ChatId);
                error.setText("ERROR : Selezionare il Dipartimento prima del corso!");
                execute(error);
            }
            else
            {
                GetMenu = findCourse(dip, reader);
            }
        }
        else if(MenuType.equals("activity"))
        {
            dip = GetDip(ChatId);
            if(dip==null)
            {
                error.setChatId(ChatId);
                error.setText("ERROR : Selezionare il Dipartimento prima dell'Attività!");
                execute(error);
            }
            else
            {
                course=GetCourse(ChatId);
                if(course==null)
                {
                    error.setChatId(ChatId);
                    error.setText("ERROR : Selezionare il Corso prima dell'Attività!");
                    execute(error);
                }
                else
                    {
                        GetMenu = findActivity(dip, course, reader);
                    }
            }
        }
        else
        {
            return null;
        }
        List<InlineKeyboardButton> Menu = new ArrayList();
        if(GetMenu.size()<1)
        {
            return null;
        }
        for(int i=0;i<GetMenu.size();i++)
        {
            Menu.add(new InlineKeyboardButton().setText(GetMenu.get(i).nome).setCallbackData(GetMenu.get(i).value));
        }
        reader.close();
        return Menu;
    }



    //FIND ACTIVITY FROM UNITO.TXT
    private List<Option> findActivity(String dip,String course,BufferedReader reader) throws IOException {
        List<Option> Menu = new ArrayList();
        String row = reader.readLine();
        boolean find = false;
        while (!find)
        {
            while (!(row = reader.readLine()).contains("Dipartimento"))
            {

            }
            if (row.contains(dip))
                find = true;
        }
        find = false;
        while (!find)
        {
            while (!(row = reader.readLine()).contains("Corso")) {

            }
            if (row.contains(course)) {
                find = true;
            }
        }
        while(!(row=reader.readLine()).contains("}"))
        {
            if (row.contains("Attivita"))
            {
                List<String> newLine = ParseLine(row);
                Menu.add(new Option(newLine.get(0),newLine.get(1)));
            }
        }
        return Menu;

    }

    //FIND COURSE FROM UNITO.TXT
    private List<Option> findCourse(String dip,BufferedReader reader) throws IOException {
        List<Option> Menu = new ArrayList();
        String row = reader.readLine();
        boolean find = false;
        while (!find)
        {
            while (!(row = reader.readLine()).contains("Dipartimento"))
            {

            }
            if (row.contains(dip))
                find = true;
        }
        while(!(row=reader.readLine()).contains("_"))
        {
            if(row.contains("Corso"))
            {
                List<String> newLine = ParseLine(row);
                Menu.add(new Option(newLine.get(0), newLine.get(1)));
            }
        }
        return Menu;
    }

    //FIND DIPARTIMENTS FROM UNITO.TXT
    private List<Option> findDip(BufferedReader reader) throws IOException {
        List<Option> Menu = new ArrayList();
        String row = reader.readLine();
        while((row=reader.readLine())!=null)
        {
            if (row.contains("Dipartimento"))
            {
                List<String> newLine = ParseLine(row);
                Menu.add(new Option(newLine.get(0),newLine.get(1)));
            }
        }
        return Menu;
    }

    public String DeleteSpace(String line)
    {
        String newLine = "";
        char[] linearray = line.toCharArray();
        for(int i=0;i<linearray.length;i++)
        {
            if(linearray[i]!=' ')
            {
                newLine = newLine+linearray[i];
            }
        }
        return newLine;
    }

    //Funzione per ottenere una lista di 2 elementi di cui il primo è il nome ed il secondo il value
    public List<String> ParseLine(String line)
    {
        List<String> newList = new ArrayList();
        String[]linearray = line.split(":");
        newList.add(linearray[1]);
        newList.add(DeleteSpace(linearray[2]));
        return newList;
    }

    //Funzione di creazione delle RIGHE della INLINE KEYBOARD
    public List<List<InlineKeyboardButton>> InlineK(List<InlineKeyboardButton> Menu,int numrows) //Funziona -> Metodo che ritorna l'array di Righe
    {
        List<List<InlineKeyboardButton>> rows = new ArrayList(); //Array of Array of InKButtons -> Tutte le righe
        List<InlineKeyboardButton> row = new ArrayList();
        int calc = Menu.size()/numrows; //Calcolo per ottenere a che punto del ciclo cambiare riga
        if(calc<1)
            calc=1;
        int change = calc;
        for(int i=0;i<Menu.size();i++) //Ciclo riempimento row
        {
            if(i==change) //se i raggiunge il massimo di elementi di una riga
            {
                rows.add(row); //aggiungo la riga
                row = new ArrayList(); //Riga singola diventa una riga vuota
                change=change+calc;
            }
            row.add(Menu.get(i)); //Aggiungo il prossimo elemento alla lista
        }
        rows.add(row); //Alla fine aggiungo l'ultima riga
        return rows; //Ritorno l'array di array di bottoni
    }


    private void PrintTab(Chat currentchat,String data) throws Exception {
        String dip = null;
        String course = null;
        BufferedReader reader = new BufferedReader(new FileReader("Chat.txt"));
        String line = null;
        while ((line = reader.readLine()) != null) {
            if (line.contains(Long.toString(currentchat.getId()))) {
                line = reader.readLine();
                String[] temp = line.split(":");
                dip = temp[1];
                line = reader.readLine();
                temp = line.split(":");
                course = temp[1];
            }
        }
        Document Result = Jsoup.connect("https://esse3.unito.it/ListaAppelliOfferta.do") //Richiesta di Post
                .data("fac_id", dip)
                .data("cds_id", course)
                .data("ad_id", data)
                .data("btnSubmit", "Avvia Ricerca")
                .userAgent("Mozilla")
                .post(); //Get the full HTML document
        Elements table = Result.select("[class=detail_table]");
        Elements tr = table.get(0).children().get(0).children();
        Elements Title = tr.get(0).children();
        List<Elements> row = new ArrayList();
        for (int i = 2; i < tr.size(); i++) {
            row.add(tr.get(i).children());
        }
        String msg = " _____________________________________\n";
        List<Element>singlerow= new ArrayList();
        for (int i = 0; i < row.size(); i++)
        {
            msg=msg+"Appello numero "+i+1+". \n\n";
            for(int j=0;j<row.get(i).size();j++)
            {
                if (j != 3)
                    singlerow.add(row.get(i).get(j));
            }
            for(int j=0;j<singlerow.size();j++) // 0 = Attività Didattica ; 1 = Periodo Iscrizioni ; 2 = Data e Ora ; 3 = Docenti; 4 = Numero Iscrizioni
            {
                if(j==3)
                {
                    msg = msg+"<b>Professori : </b><i>";
                    Elements names = singlerow.get(j).children().get(0).children().get(0).children();
                    for(int z=0;z<names.size();z++)
                    {
                        if (z != names.size() - 1)
                            msg = msg + names.get(z).text() + ", ";
                        else
                            msg = msg + names.get(z).text();
                    }
                    msg=msg+"</i>\n\n ";
                }
                else
                {
                    if(j==0)
                        msg = msg+"<i>"+singlerow.get(j).text() + "</i> \n\n";
                    else if(j==1)
                        msg = msg+"<b>Iscrizioni (dal-al) : </b><i>"+singlerow.get(j).text() + "</i> \n\n ";
                    else if(j==2)
                        msg = msg+"<b>Esame (giorno-ora) : </b><i>"+singlerow.get(j).text() + "</i> \n\n ";
                    else
                        msg=msg+"<b>Numero Iscritti : </b><i>"+singlerow.get(j).text()+"</i>";
                }
            }
            msg=msg+"\n _____________________________________ \n";
            singlerow.clear();
        }
        SendMessage message = new SendMessage();
        message.setChatId(currentchat.getId());
        message.setText(msg); //DA METTERE A POSTO LA FORMATTAZIONE
        message.setParseMode("html");
        reader.close();
        execute(message);
    }

    public String getBotUsername() {
        return "UNITO_INFO_bot";
    }

    public String getBotToken() {
        return "730744147:AAGekwGW9b95nvxsPcuZ2yC9qoJMBJnDQss";
    }
}
