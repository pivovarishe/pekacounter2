/**
 * Created by Pivovarishe on 08.04.2016.
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
public class parser {
    public static void main(String[] args) throws ClassNotFoundException, SQLException, IOException {
        // TODO Auto-generated method stub
        Class.forName("com.mysql.jdbc.Driver");
        //подключаем драйвер MySQL
        Connection conn = DriverManager.getConnection("jdbc:mysql://192.168.1.7:3306/database", "username", "password");
        //192.168.1.7 - имя хоста, можно подставить хоть localhost
        //database - имя базы данных для экспорта
        //username, password - имя пользователя и пароль для MySQL
        Statement st = conn.createStatement();
        URL connection = null;
        String[] replacements = new String[10];
        String host = null;
        String port = null;
        String anon_level = null;
        String country = null;
        int cursor = 0;
        HttpURLConnection urlconn = null;
        int n = 1;
        while (n <= 50)
        {
            if (n < 10)
            {
                connection = new URL("www.samair.ru/proxy/ip-address-0"; +n+".htm");
            }
            else
            {
                connection = new URL("www.samair.ru/proxy/ip-address-";+n+".htm");
            }

            System.out.println("Starting page: "+Integer.toString(n));
            urlconn = (HttpURLConnection) connection.openConnection();
            urlconn.setRequestMethod("GET");
            urlconn.connect();
            //посылаем GET запрос на список проксей samair'а
            java.io.InputStream in = urlconn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String text = null;
            String line = null;
            while ((line = reader.readLine()) != null)
            {
                text += line;
            }
            //парсим текст страницы
            replacements = text.substring(text.indexOf("<script src=\"http://samair.ru:81/js/m.js" type="text/javascript">) + "<script src=\"http://samair.ru:81/js/m.js" type="text/javascript">.length(), text.indexOf("</script></head>")).split(";");
            //на самаире, возможно, в целях защиты от парсеров порты в списке выводятся javascript'ом
            //в начале страницы рандомом задаются 10 переменных для каждой цыфры, затем они скриптом же и выводятся в таблицу
            //replacements - как раз массив этих переменных
            cursor = text.indexOf("<tr><td>");
            while (cursor != -1)
            {
                cursor += "<tr><td>".length();
                host = text.substring(cursor, text.indexOf("<script type=\"text/javascript\">", cursor));
                //host - адрес прокси сервера
                port = text.substring(text.indexOf(">document.write(\":\"+", cursor) + ">document.write(\":\"+".length(), text.indexOf(")</script>" , cursor));
                port = removeChar(port, '+');
                for (int i = 0; i<10; i++)
                {
                    port = port.replaceAll(replacements[i].split("=")[0], replacements[i].split("=")[1]);
                    //подставляем вместо букв циферки
                }
                //port - порт сервера
                cursor = text.indexOf("</td><td>", cursor) + "</td><td>".length();
                anon_level = text.substring(cursor, text.indexOf("</td><td>", cursor));
                cursor = text.indexOf("</td><td>", cursor) + "</td><td>".length();
                cursor = text.indexOf("</td><td>", cursor) + "</td><td>".length();
                country = text.substring(cursor, text.indexOf("</td></tr>", cursor));
                //получаем остальную лабуду - тип сервера и страна, не пропадать же траффику зря) хотя они и вряд ли понадобятся
                ResultSet rs = st.executeQuery("select host, port from proxies where host = '"+host+"' and port = '"+port+"'");
                if (!rs.next())
                {
                    st.executeUpdate("INSERT INTO proxies (host, port, anon_level, country) VALUES ('"+host+"', '"+port+"', '"+anon_level+"', '"+country+"')");
                    System.out.println("Added: "+host+":"+port);
                    //Если такого хоста и порта в базе еще нету, то вносим его туда
                }
                cursor = text.indexOf("<tr><td>", cursor);
            }

            n++;

        }

        st.close();
        conn.close();
    }

    public static String removeChar(String s, char c) {
        String r = "";
        for (int i = 0; i < s.length(); i ++) {
            if (s.charAt(i) != c) r += s.charAt(i);
        }
        return r;
    }

}
}
