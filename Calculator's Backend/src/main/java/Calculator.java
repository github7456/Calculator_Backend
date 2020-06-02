import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

// Extend HttpServlet class
public class Calculator extends HttpServlet {
    private final static String FIRST_NUMBER = "f";
    private final static String SECOND_NUMMBER = "s";
    private final static String OPERATOR = "o";

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String firstNumber = request.getParameter(FIRST_NUMBER);
        if (firstNumber == null || firstNumber.length() == 0) {
            throw new IllegalArgumentException("Missing parameter: " + FIRST_NUMBER);
        }

        String secondNumber = request.getParameter(SECOND_NUMMBER);
        if (secondNumber == null || secondNumber.length() == 0) {
            throw new IllegalArgumentException("Missing parameter: " + SECOND_NUMMBER);
        }

        String operatorStr = request.getParameter(OPERATOR);
        if (operatorStr == null || operatorStr.length() == 0) {
            throw new IllegalArgumentException("Illegal parameter: " + OPERATOR);
        }

        float first = Float.parseFloat(firstNumber);
        float second = Float.parseFloat(secondNumber);
        int operator = Integer.parseInt(operatorStr);
        float ans = calculate(first, second, operator);

        response.setContentType("text");

        // Actual logic goes here.
        PrintWriter out = response.getWriter();
        out.println(ans);
    }

    private float calculate(float first, float second, int operator) throws ServletException {
        switch (operator) {
            case 1:
                return first + second;
            case 2:
                return first - second;
            case 3:
                return first * second;
            case 4:
                return first / second;
            default:
                throw new ServletException("Unknown operator " + operator);
        }
    }
}