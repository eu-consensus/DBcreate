package servlets;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;
import static java.sql.Types.NULL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import methods.*;
import methods.methods.polComparator;
import methods.methods.polComparator2;

@WebServlet(name = "Upload", urlPatterns = {"/Upload"})
@MultipartConfig
public class Upload extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        Part filePart = request.getPart("file");
        String filename = getFilename(filePart);
        InputStream filecontent = filePart.getInputStream();

        int objectivecount = Integer.parseInt(request.getParameter("objectivecount"));
        String[] policyname = request.getParameterValues("policyname");
        boolean hasname = false;
        if (policyname != null) {
            hasname = true;
        }

        String tablename = request.getParameter("table");
        String minmax = request.getParameter("minmax");
        String weig = request.getParameter("weights");

        boolean[] myminmax = methods.minmax(minmax);

        List<policy> mypol = createdata(filecontent, objectivecount, hasname, weig);
        Collections.sort(mypol, new polComparator());
        List<policy> mypol1 = methods.paretoM(mypol, myminmax);
        List<policy> mypol2 = methods.dominationBYcategory(mypol1);
        Collections.sort(mypol2, new polComparator2());
        List<policy> mypol3 = methods.nsga2FH(mypol2, myminmax);
        Collections.sort(mypol3, new polComparator2());

        String line = "";
        String splitBy = ",";
        String dbName = "mytestDB";
        String url = "jdbc:mysql://localhost:3306/";
        String driver = "com.mysql.jdbc.Driver";
        String userName = "root";
        String password = "";
        Statement stmt = null;
        ResultSet rs = null;
        String sql = "";

        String obj = "";
        String objn = "";

        for (int num = 0; num < objectivecount; num++) {
            obj += "obj" + Integer.toString(num) + " DOUBLE, ";
        }
        for (int num = 0; num < objectivecount; num++) {
            objn += "obj" + Integer.toString(num) + ",";
        }

        sql = "CREATE TABLE IF NOT EXISTS " + tablename
                + "(id INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY, "
                + " policy VARCHAR(255) UNIQUE, "
                + obj
                + " distance DOUBLE,"
                + " dominatedbycategory int(30),"
                + " dominatedbypool int(30),"
                + " rank int(30),"
                + " myorder varchar(12),"
                + " weights varchar(255),"
                + " chosen int(30) DEFAULT 0,"
                + " liked int(30)DEFAULT 0, "
                + " objscore DOUBLE,"
                + " prefscore DOUBLE)";

        //  System.out.println(sql);
        try {

            Class.forName(driver).newInstance();
            Connection conn = DriverManager.getConnection(url + dbName, userName, password);
            //    System.out.println("Connected database successfully...");
            stmt = conn.createStatement();
            stmt.executeUpdate(sql);
            //    System.out.println("Created table in given database...");
            for (policy pol : mypol3) {
                String addobj = "";
                for (double obj1 : pol.getObjectives()) {
                    addobj += "?,";
                }
                String query = "INSERT INTO " + tablename + " (policy," + objn + "distance,dominatedbycategory,dominatedbypool,rank,myorder,weights,chosen,liked,objscore,prefscore) VALUES(?," + addobj + "?,?,?,?,?,?,?,?,?,?)";
                System.out.println(query);
                PreparedStatement statement = conn.prepareStatement(query);
                //  statement.setString(1, tablename);
                statement.setString(1, pol.getPolicyName());
                for (int num = 0; num < objectivecount; num++) {
                    statement.setDouble(num + 2, pol.getObjectives()[num]);
                }
                statement.setDouble(objectivecount + 2, pol.getDistance());
                statement.setInt(objectivecount + 3, pol.getDominatedbycategory());
                statement.setInt(objectivecount + 4, pol.getDominated());
                statement.setInt(objectivecount + 5, pol.getRank());
                statement.setString(objectivecount + 6, pol.getOrder());
                if (!pol.getWeights().equals("")) {
                    statement.setString(objectivecount + 7, pol.getWeights());
                } else {
                    statement.setNull(objectivecount + 7, Types.VARCHAR);
                }
                statement.setInt(objectivecount + 8, 0);
                statement.setInt(objectivecount + 9, 0);
                statement.setInt(objectivecount + 10, 0);
                statement.setInt(objectivecount + 11, 0);
//System.out.println(statement);
                statement.executeUpdate();
            }

            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        ServletContext context = getServletContext();
        RequestDispatcher dispatcher = context.getRequestDispatcher("/dbLoad.jsp");
        dispatcher.forward(request, response);

    }

    private static String getFilename(Part part) {
        for (String cd : part.getHeader("content-disposition").split(";")) {
            if (cd.trim().startsWith("filename")) {
                String filename = cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
                return filename.substring(filename.lastIndexOf('/') + 1).substring(filename.lastIndexOf('\\') + 1);
            }
        }
        return null;
    }

    private static String readString(InputStream is) throws IOException {
        char[] buf = new char[2048];
        Reader r = new InputStreamReader(is, "UTF-8");
        StringBuilder s = new StringBuilder();
        while (true) {
            int n = r.read(buf);
            if (n < 0) {
                break;
            }
            s.append(buf, 0, n);
        }
        return s.toString();
    }

    private static List<policy> createdata(InputStream is, int number, boolean hasname, String weight) throws IOException {

        List<policy> mypolicy = new ArrayList<>();
        char[] buf = new char[2048];
        String line = "";
        String splitBy = ",";

        BufferedReader r = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        while ((line = r.readLine()) != null) {
            //minimization (-) maximization(+)
            String[] policy = line.split(splitBy);
            policy newpol = new policy(number, hasname);
            int u = 0;
            double[] data = new double[number];
            for (int i = 0; i < policy.length; i++) {
                //if a policy name is provided then add it else create a Unique id
                if (hasname && i == 0) {
                    newpol.setPolicyName(policy[0]);
                    continue;
                }

                data[u] = Double.parseDouble(policy[i]);
                u++;
            }
            newpol.setObjectives(data);
            newpol.setDistance();
            newpol.setOrder(number);
            newpol.setWeights(weight);
            newpol.changewithWeights();
            mypolicy.add(newpol);
        }
        return mypolicy;
    }
}
