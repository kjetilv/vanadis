/*
 * Copyright 2009 Kjetil Valstadsve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package vanadis.modules.scripting;

import vanadis.core.collections.Generic;
import vanadis.core.io.Closeables;
import vanadis.core.io.IO;
import vanadis.core.io.IORuntimeException;
import vanadis.core.lang.Strings;
import vanadis.core.lang.ToString;
import vanadis.core.system.VM;
import vanadis.osgi.Context;
import vanadis.osgi.Reference;
import vanadis.services.scripting.ScriptingSession;
import vanadis.services.scripting.ScriptingSessions;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Map;

class ScriptingServlet extends HttpServlet {

    private String indexHtml;

    private final Map<String, String> lastScriptingSession = Generic.map();

    private final ScriptingSessions scriptingSessions;

    private final Context context;

    ScriptingServlet(ScriptingSessions scriptingSessions, Context context) {
        this.scriptingSessions = scriptingSessions;
        this.context = context;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {
        doGet(req, res);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) {
        res.setContentType(TEXT_HTML);
        PrintStream ps = printStream(res);
        try {
            ps = process(ps, req);
        } catch (Throwable e) {
            printError(ps, e);
        } finally {
            Closeables.close(ps);
        }
    }

    private PrintStream process(PrintStream printStream, HttpServletRequest req) {
        String id = req.getSession().getId();
        String sessionName = sessionName(req);
        String action = action(req);
        storeSession(id, sessionName);
        Object actionResult = null;
        try {
            actionResult = dispatch(action, sessionName, req);
        } catch (Oops e) {
            if (e.getCause() != null) {
                print(e.getCause());
            } else {
                actionResult = e.getMessage();
            }
        }
        startScreen(id, actionResult, printStream);
        return printStream;
    }

    private Object dispatch(String action, String sessionName, HttpServletRequest req) {
        if (action == null) {
            return null;
        }
        if (action.equalsIgnoreCase("eval")) {
            return eval(sessionName, req);
        }
        if (action.equalsIgnoreCase("create")) {
            return createSession(sessionName, req);
        }
        if (action.equalsIgnoreCase("destrory")) {
            return destroySession(sessionName);
        }
        throw new Oops("Unknown action: " + action);
    }

    private String destroySession(String name) {
        try {
            scriptingSessions.clearSession(name);
            return "OK, gone";
        } catch (Throwable e) {
            return print(e);
        }
    }

    private void storeSession(String id, String session) {
        if (session != null && id != null) {
            lastScriptingSession.put(id, session);
        }
    }

    private String createSession(String name, HttpServletRequest req) {
        try {
            String serviceName = serviceName(req);
            String binding = binding(req);
            Reference<?> service = reference(req, serviceName);
            ScriptingSession session = newSession(name, language(req), serviceName, binding, service);
            return session.toString();
        } catch (Oops e) {
            throw e;
        } catch (Throwable e) {
            throw new Oops(e);
        }
    }

    private Reference<?> reference(HttpServletRequest req, String serviceName) {
        if (serviceName != null) {
            Reference<?> reference = Services.getReference(context, serviceName, serviceId(req));
            if (reference == null) {
                throw new Oops("No service " + serviceName);
            }
            return reference;
        }
        return null;
    }

    private ScriptingSession newSession(String name, String language,
                                        String serviceName, String binding,
                                        Reference<?> service) {
        return scriptingSessions.newSession
                (name, language, service, Strings.isEmpty(binding) ? serviceName : binding);
    }

    private void startScreen(String id, Object value, PrintStream printStream) {
        printStream.print(getIndexHtml().replace
                (SESSIONS, sessionList(id)).replace
                (LAST_VALUE, renderValue(value)));
    }

    private String sessionList(String id) {
        String last = id == null ? null : lastScriptingSession.get(id);
        ScriptingSessions scriptingSessions = this.scriptingSessions;
        StringBuilder sb = new StringBuilder();
        for (String name : scriptingSessions.sessionNames()) {
            String selected = name.equals(last) ? " selected=\"selected\"" : "";
            sb.append("<option value=\"").append(name).append("\"").append(selected).append(">");
            sb.append(name);
            sb.append("</option>").append(VM.LN);
        }
        return sb.toString();
    }

    private String getIndexHtml() {
        if (indexHtml == null) {
            ByteArrayOutputStream bytes = readFile();
            indexHtml = htmlFrom(bytes);
        }
        return indexHtml;
    }

    private Object eval(String name, HttpServletRequest req) {
        ScriptingSession session = scriptingSessions.getSession(name);
        if (session == null) {
            session = createSessionForService(name, req);
        }
        try {
            return session.eval(script(req));
        } catch (Throwable e) {
            return print(e);
        }
    }

    private ScriptingSession createSessionForService(String name, HttpServletRequest req) {
        return newSession(name, "piji", name, "service", reference(req, name));
    }

    private static String print(Throwable e) {
        return "<pre>" + ToString.throwable(e) + "</pre>";
    }

    private static final String ACTION = "action";

    private static final String NAME = "name";

    private static final String LANG = "lang";

    private static final String SERVICE_ID = "serviceId";

    private static final String SERVICE = "service";

    private static final String BINDING = "binding";

    private static final String SCRIPT = "script";

    private static final String UTF_8 = "UTF-8";

    private static final String SESSIONS = "<!-- SESSIONS -->";

    private static final String LAST_VALUE = "<!-- LAST VALUE -->";

    private static final String TEXT_HTML = "text/html";

    private static final String START_PAGE = "index.html";

    private static final long serialVersionUID = -7808359180149550468L;

    private static ByteArrayOutputStream readFile() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(bytes);
        try {
            IO.copy(resourceStream(), printStream);
        } finally {
            Closeables.close(printStream);
        }
        return bytes;
    }

    private static InputStream resourceStream() {
        ClassLoader loader = ScriptingServlet.class.getClassLoader();
        InputStream stream = loader.getResourceAsStream(START_PAGE);
        if (stream == null) {
            throw new IllegalStateException("Unable to find resource " + START_PAGE + " in " + loader);
        }
        return stream;
    }

    private static String htmlFrom(ByteArrayOutputStream bytes) {
        try {
            return new String(bytes.toByteArray(), UTF_8);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(UTF_8 + " not supported", e);
        }
    }

    private PrintStream printStream(HttpServletResponse res) {
        try {
            return new PrintStream(res.getOutputStream());
        } catch (IOException e) {
            throw new IORuntimeException(this + " failed to open result stream on " + res, e);
        }
    }

    private static void printError(PrintStream ps, Throwable e) {
        ps.println("<html><title>Ooops</title><body><pre>");
        e.printStackTrace(ps);
        ps.println("</pre></body></html>");
    }

    private static String renderValue(Object value) {
        if (value == null) {
            return LAST_VALUE;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<code><strong>").append(value).append("</strong></code>");
        return sb.toString();
    }

    private static String action(HttpServletRequest req) {
        return HttpReqs.getPar(req, ACTION);
    }

    private static String serviceName(HttpServletRequest req) {
        return HttpReqs.getPar(req, SERVICE);
    }

    private static String binding(HttpServletRequest req) {
        return HttpReqs.getPar(req, BINDING);
    }

    private static String language(HttpServletRequest req) {
        return HttpReqs.getPar(req, LANG);
    }

    private static String sessionName(HttpServletRequest req) {
        return HttpReqs.getPar(req, NAME);
    }

    private static String script(HttpServletRequest req) {
        return HttpReqs.getPar(req, SCRIPT);
    }

    @Override
    public String toString() {
        return ToString.of(this, "context", context, "sessions", scriptingSessions);
    }

    static int serviceId(HttpServletRequest req) {
        String par = HttpReqs.getPar(req, SERVICE_ID);
        return par == null ? -1 : Integer.parseInt(par);
    }

    private final class Oops extends RuntimeException {
        private static final long serialVersionUID = -2947080481652367471L;

        private Oops(String msg) {
            super(msg);
        }

        private Oops(Throwable cause) {
            super(cause);
        }
    }
}
