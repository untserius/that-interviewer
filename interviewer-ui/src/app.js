import { useState, useEffect, useRef, useCallback } from "react";

// ── Speech Recognition Hook ───────────────────────────────────────────────────
function useSpeechRecognition({ onResult, onEnd }) {
  const recognitionRef = useRef(null);
  const [listening, setListening] = useState(false);
  const [supported, setSupported] = useState(false);

  useEffect(() => {
    const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
    if (!SpeechRecognition) return;
    setSupported(true);

    const rec = new SpeechRecognition();
    rec.continuous = true;
    rec.interimResults = true;
    rec.lang = "en-US";

    rec.onresult = (e) => {
      const transcript = Array.from(e.results)
        .map(r => r[0].transcript)
        .join("");
      onResult(transcript);
    };

    rec.onend = () => {
      setListening(false);
      if (onEnd) onEnd();
    };

    rec.onerror = (e) => {
      console.warn("Speech error:", e.error);
      setListening(false);
    };

    recognitionRef.current = rec;
    return () => rec.abort();
  }, []);

  const start = useCallback(() => {
    recognitionRef.current?.start();
    setListening(true);
  }, []);

  const stop = useCallback(() => {
    recognitionRef.current?.stop();
    setListening(false);
  }, []);

  const toggle = useCallback(() => {
    listening ? stop() : start();
  }, [listening, start, stop]);

  return { listening, supported, toggle, stop };
}

// Dynamically points to same host the UI is served from, port 8080
// Works for localhost AND other devices on the same network
const API = process.env.REACT_APP_API_URL || `http://${window.location.hostname}:8080`;

const ROLES = ["Backend Engineer", "Frontend Engineer", "Fullstack Engineer", "DevOps Engineer"];
const EXPERIENCES = ["0-2", "1-3", "3-5", "5+"];

// ── Styles ────────────────────────────────────────────────────────────────────

const css = `
  *, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }

  :root {
    --bg: #0a0a0f;
    --surface: #111118;
    --border: #1e1e2e;
    --accent: #e8ff47;
    --accent2: #47ffce;
    --text: #f0f0f0;
    --muted: #555570;
    --danger: #ff4747;
    --good: #47ffce;
    --font-display: 'Syne', sans-serif;
    --font-mono: 'JetBrains Mono', monospace;
  }

  body {
    background: var(--bg);
    color: var(--text);
    font-family: var(--font-display);
    min-height: 100vh;
    overflow-x: hidden;
  }

  .noise {
    position: fixed; inset: 0; pointer-events: none; z-index: 0;
    background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 256 256' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='noise'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.9' numOctaves='4' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23noise)' opacity='0.03'/%3E%3C/svg%3E");
    opacity: 0.4;
  }

  .page {
    position: relative; z-index: 1;
    min-height: 100vh;
    display: flex; flex-direction: column; align-items: center; justify-content: center;
    padding: 2rem;
  }

  /* ── SETUP PAGE ── */
  .setup-container {
    width: 100%; max-width: 520px;
    animation: fadeUp 0.6s ease forwards;
  }

  .logo {
    font-size: 0.75rem; font-weight: 700; letter-spacing: 0.3em;
    color: var(--accent); text-transform: uppercase;
    font-family: var(--font-mono);
    margin-bottom: 3rem;
  }

  .setup-title {
    font-size: clamp(2.5rem, 6vw, 4rem);
    font-weight: 800; line-height: 1.05;
    margin-bottom: 0.5rem;
  }

  .setup-title span { color: var(--accent); }

  .setup-sub {
    color: var(--muted); font-size: 1rem;
    font-family: var(--font-mono); font-weight: 300;
    margin-bottom: 3rem;
  }

  .field { margin-bottom: 1.5rem; }

  .field label {
    display: block;
    font-size: 0.7rem; font-weight: 700; letter-spacing: 0.2em;
    text-transform: uppercase; color: var(--muted);
    font-family: var(--font-mono);
    margin-bottom: 0.75rem;
  }

  .role-grid {
    display: grid; grid-template-columns: 1fr 1fr;
    gap: 0.5rem;
  }

  .chip {
    padding: 0.75rem 1rem;
    border: 1px solid var(--border);
    background: var(--surface);
    color: var(--muted);
    font-family: var(--font-display);
    font-size: 0.85rem; font-weight: 600;
    cursor: pointer;
    transition: all 0.15s;
    text-align: center;
  }

  .chip:hover { border-color: var(--accent); color: var(--text); }
  .chip.active { border-color: var(--accent); color: var(--accent); background: rgba(232,255,71,0.06); }

  .exp-grid {
    display: grid; grid-template-columns: repeat(4, 1fr);
    gap: 0.5rem;
  }

  .start-btn {
    width: 100%; padding: 1.1rem;
    margin-top: 2rem;
    background: var(--accent);
    color: #0a0a0f;
    border: none; cursor: pointer;
    font-family: var(--font-display);
    font-size: 1rem; font-weight: 800;
    letter-spacing: 0.05em;
    transition: all 0.15s;
    position: relative; overflow: hidden;
  }

  .start-btn:hover { background: #f5ff80; transform: translateY(-1px); }
  .start-btn:disabled { opacity: 0.4; cursor: not-allowed; transform: none; }

  /* ── QUESTION PAGE ── */
  .q-container {
    width: 100%; max-width: 720px;
    animation: fadeUp 0.4s ease forwards;
  }

  .q-header {
    display: flex; align-items: center; justify-content: space-between;
    margin-bottom: 2.5rem;
  }

  .q-progress-wrap {
    flex: 1;
    height: 3px; background: var(--border);
    margin: 0 1.5rem;
    position: relative; overflow: hidden;
  }

  .q-progress-bar {
    height: 100%; background: var(--accent);
    transition: width 0.4s ease;
  }

  .q-counter {
    font-family: var(--font-mono); font-size: 0.75rem;
    color: var(--muted); white-space: nowrap;
  }

  .q-role-badge {
    font-family: var(--font-mono); font-size: 0.65rem; font-weight: 500;
    letter-spacing: 0.2em; text-transform: uppercase;
    color: var(--accent2); white-space: nowrap;
  }

  .q-difficulty {
    display: inline-block;
    font-family: var(--font-mono); font-size: 0.65rem; font-weight: 500;
    letter-spacing: 0.15em; text-transform: uppercase;
    padding: 0.25rem 0.6rem;
    border: 1px solid currentColor;
    margin-bottom: 1.2rem;
  }

  .q-difficulty.junior { color: var(--good); }
  .q-difficulty.mid    { color: var(--accent); }
  .q-difficulty.senior { color: #ff9447; }

  .q-text {
    font-size: clamp(1.3rem, 3vw, 1.7rem);
    font-weight: 700; line-height: 1.4;
    margin-bottom: 2rem;
  }

  .q-concepts {
    display: flex; flex-wrap: wrap; gap: 0.4rem;
    margin-bottom: 2rem;
  }

  .concept-tag {
    font-family: var(--font-mono); font-size: 0.65rem;
    padding: 0.2rem 0.5rem;
    background: rgba(232,255,71,0.07);
    border: 1px solid rgba(232,255,71,0.2);
    color: var(--accent);
  }

  .q-answer-wrap { position: relative; margin-bottom: 1.5rem; }

  .q-answer {
    width: 100%; min-height: 160px;
    padding: 1.2rem 1.2rem 2.8rem 1.2rem;
    background: var(--surface);
    border: 1px solid var(--border);
    color: var(--text);
    font-family: var(--font-mono); font-size: 0.9rem; font-weight: 300;
    resize: vertical; outline: none;
    transition: border-color 0.15s;
    line-height: 1.7;
  }

  .q-answer:focus { border-color: var(--accent); }
  .q-answer.listening { border-color: var(--danger); animation: borderPulse 1.2s infinite; }
  .q-answer::placeholder { color: var(--muted); }

  .char-count {
    position: absolute; bottom: 0.75rem; right: 0.75rem;
    font-family: var(--font-mono); font-size: 0.65rem; color: var(--muted);
  }

  .mic-btn {
    position: absolute; bottom: 0.6rem; left: 0.75rem;
    display: flex; align-items: center; gap: 0.4rem;
    padding: 0.28rem 0.65rem;
    background: transparent;
    border: 1px solid var(--border);
    color: var(--muted);
    font-family: var(--font-mono); font-size: 0.65rem;
    cursor: pointer; transition: all 0.15s;
  }

  .mic-btn:hover:not(.unavailable) { border-color: var(--accent2); color: var(--accent2); }
  .mic-btn.active { border-color: var(--danger); color: var(--danger); background: rgba(255,71,71,0.08); }
  .mic-btn.unavailable { opacity: 0.25; cursor: not-allowed; }

  .mic-dot { width: 5px; height: 5px; border-radius: 50%; background: currentColor; animation: pulse 1s infinite; }

  @keyframes borderPulse {
    0%, 100% { border-color: var(--danger); }
    50% { border-color: rgba(255,71,71,0.25); }
  }

  .q-actions { display: flex; gap: 1rem; align-items: center; }

  .next-btn {
    flex: 1; padding: 1rem;
    background: var(--accent); color: #0a0a0f;
    border: none; cursor: pointer;
    font-family: var(--font-display); font-size: 0.95rem; font-weight: 800;
    transition: all 0.15s;
  }

  .next-btn:hover { background: #f5ff80; }
  .next-btn:disabled { opacity: 0.4; cursor: not-allowed; }

  .skip-btn {
    padding: 1rem 1.5rem;
    background: transparent; color: var(--muted);
    border: 1px solid var(--border); cursor: pointer;
    font-family: var(--font-display); font-size: 0.85rem; font-weight: 600;
    transition: all 0.15s;
  }

  .skip-btn:hover { border-color: var(--muted); color: var(--text); }

  .scoring-indicator {
    display: flex; align-items: center; gap: 0.5rem;
    font-family: var(--font-mono); font-size: 0.7rem; color: var(--muted);
  }

  .dot { width: 6px; height: 6px; border-radius: 50%; background: var(--accent2); animation: pulse 1.5s infinite; }

  /* ── LOADING ── */
  .loading-wrap {
    display: flex; flex-direction: column; align-items: center; gap: 1.5rem;
  }

  .spinner {
    width: 40px; height: 40px;
    border: 2px solid var(--border);
    border-top-color: var(--accent);
    border-radius: 50%;
    animation: spin 0.8s linear infinite;
  }

  .loading-text {
    font-family: var(--font-mono); font-size: 0.8rem; color: var(--muted);
  }

  /* ── SUMMARY PAGE ── */
  .summary-container {
    width: 100%; max-width: 820px;
    animation: fadeUp 0.5s ease forwards;
  }

  .summary-hero {
    text-align: center; margin-bottom: 4rem;
    padding-bottom: 3rem;
    border-bottom: 1px solid var(--border);
  }

  .grade-ring {
    width: 120px; height: 120px;
    border-radius: 50%;
    border: 3px solid var(--accent);
    display: flex; align-items: center; justify-content: center;
    margin: 0 auto 1.5rem;
    position: relative;
  }

  .grade-letter {
    font-size: 3rem; font-weight: 800; color: var(--accent);
    line-height: 1;
  }

  .summary-score {
    font-size: 1rem; font-family: var(--font-mono); color: var(--muted);
    margin-bottom: 0.5rem;
  }

  .summary-title {
    font-size: clamp(1.8rem, 4vw, 2.8rem); font-weight: 800;
    margin-bottom: 0.5rem;
  }

  .summary-meta {
    font-family: var(--font-mono); font-size: 0.75rem; color: var(--muted);
    letter-spacing: 0.1em;
  }

  .results-list { display: flex; flex-direction: column; gap: 1.5rem; }

  .result-card {
    border: 1px solid var(--border);
    background: var(--surface);
    overflow: hidden;
    transition: border-color 0.15s;
  }

  .result-card:hover { border-color: var(--muted); }

  .result-header {
    display: flex; align-items: center; justify-content: space-between;
    padding: 1.2rem 1.5rem;
    cursor: pointer;
    user-select: none;
  }

  .result-num {
    font-family: var(--font-mono); font-size: 0.65rem;
    color: var(--muted); margin-bottom: 0.3rem;
    letter-spacing: 0.15em; text-transform: uppercase;
  }

  .result-question { font-size: 0.95rem; font-weight: 700; line-height: 1.4; }

  .score-badge {
    flex-shrink: 0; margin-left: 1rem;
    font-family: var(--font-mono); font-size: 1.1rem; font-weight: 500;
    width: 56px; text-align: center;
  }

  .score-badge.high { color: var(--good); }
  .score-badge.mid  { color: var(--accent); }
  .score-badge.low  { color: var(--danger); }

  .result-body {
    padding: 0 1.5rem 1.5rem;
    border-top: 1px solid var(--border);
  }

  .answer-section { margin-top: 1.2rem; }

  .answer-label {
    font-family: var(--font-mono); font-size: 0.6rem; font-weight: 500;
    letter-spacing: 0.2em; text-transform: uppercase;
    color: var(--muted); margin-bottom: 0.5rem;
  }

  .answer-text {
    font-family: var(--font-mono); font-size: 0.82rem; font-weight: 300;
    line-height: 1.7; color: var(--text);
    padding: 1rem; background: rgba(255,255,255,0.02);
    border-left: 2px solid var(--border);
  }

  .ideal-text { border-left-color: var(--accent2); color: #c0c0d0; }

  .score-breakdown {
    display: flex; gap: 1rem; margin-top: 1.2rem; flex-wrap: wrap;
  }

  .breakdown-item {
    flex: 1; min-width: 100px;
    padding: 0.75rem;
    background: rgba(255,255,255,0.02);
    border: 1px solid var(--border);
    text-align: center;
  }

  .breakdown-val {
    font-family: var(--font-mono); font-size: 1.1rem; font-weight: 500;
    color: var(--accent); display: block;
  }

  .breakdown-lbl {
    font-family: var(--font-mono); font-size: 0.6rem;
    color: var(--muted); text-transform: uppercase; letter-spacing: 0.15em;
    margin-top: 0.2rem; display: block;
  }

  .matched-concepts { display: flex; flex-wrap: wrap; gap: 0.35rem; margin-top: 1rem; }

  .retry-btn {
    display: block; width: 100%; padding: 1.1rem;
    margin-top: 3rem;
    background: transparent; color: var(--accent);
    border: 1px solid var(--accent); cursor: pointer;
    font-family: var(--font-display); font-size: 1rem; font-weight: 800;
    transition: all 0.15s;
  }

  .retry-btn:hover { background: rgba(232,255,71,0.08); }

  .chevron {
    font-size: 0.7rem; color: var(--muted);
    transition: transform 0.2s;
  }

  .chevron.open { transform: rotate(180deg); }

  /* ── KEYFRAMES ── */
  @keyframes fadeUp {
    from { opacity: 0; transform: translateY(20px); }
    to   { opacity: 1; transform: translateY(0); }
  }

  @keyframes spin {
    to { transform: rotate(360deg); }
  }

  @keyframes pulse {
    0%, 100% { opacity: 1; } 50% { opacity: 0.3; }
  }
`;

// ── Score helpers ─────────────────────────────────────────────────────────────

function scoreClass(s) {
  if (s >= 0.7) return "high";
  if (s >= 0.45) return "mid";
  return "low";
}

function pct(s) {
  return `${Math.round(s * 100)}%`;
}

function gradeMessage(grade) {
  return {
    A: "Exceptional performance",
    B: "Strong answers",
    C: "Decent foundation",
    D: "Needs improvement",
    F: "Keep practicing",
  }[grade] || "";
}

// ── Components ────────────────────────────────────────────────────────────────

function SetupPage({ onStart }) {
  const [role, setRole] = useState("");
  const [experience, setExperience] = useState("");
  const [loading, setLoading] = useState(false);

  async function handleStart() {
    if (!role || !experience) return;
    setLoading(true);
    try {
      const res = await fetch(
        `${API}/session/start?role=${encodeURIComponent(role)}&experience=${encodeURIComponent(experience)}`
      );
      const data = await res.json();
      onStart(data, role, experience);
    } catch (e) {
      alert("Could not connect to API. Is Spring Boot running on port 8080?");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="page">
      <div className="setup-container">
        <div className="logo">⬡ That Interviewer</div>
        <h1 className="setup-title">
          Ready to get<br /><span>interviewed?</span>
        </h1>
        <p className="setup-sub">10 questions · scored by AI · instant feedback</p>

        <div className="field">
          <label>Your Role</label>
          <div className="role-grid">
            {ROLES.map((r) => (
              <button key={r} className={`chip ${role === r ? "active" : ""}`} onClick={() => setRole(r)}>
                {r}
              </button>
            ))}
          </div>
        </div>

        <div className="field">
          <label>Years of Experience</label>
          <div className="exp-grid">
            {EXPERIENCES.map((e) => (
              <button key={e} className={`chip ${experience === e ? "active" : ""}`} onClick={() => setExperience(e)}>
                {e} yrs
              </button>
            ))}
          </div>
        </div>

        <button
          className="start-btn"
          onClick={handleStart}
          disabled={!role || !experience || loading}
        >
          {loading ? "Starting..." : "Begin Interview →"}
        </button>
      </div>
    </div>
  );
}

function QuestionPage({ session, role, experience, onFinish }) {
  const [index, setIndex] = useState(0);
  const [answer, setAnswer] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [scores, setScores] = useState([]);
  const [speechText, setSpeechText] = useState("");
  const textareaRef = useRef(null);

  const questions = session.questions;
  const current = questions[index];
  const isLast = index === questions.length - 1;

  // Merge typed text + live speech transcript
  const { listening, supported, toggle, stop } = useSpeechRecognition({
    onResult: (transcript) => setSpeechText(transcript),
    onEnd: () => setSpeechText(""),
  });

  // Combined answer: typed base + live speech appended
  const baseAnswerRef = useRef("");
  const displayAnswer = listening
    ? baseAnswerRef.current + (baseAnswerRef.current && speechText ? " " : "") + speechText
    : answer;

  function handleMicToggle() {
    if (listening) {
      // Commit speech into the answer on stop
      const committed = baseAnswerRef.current
        + (baseAnswerRef.current && speechText ? " " : "")
        + speechText;
      setAnswer(committed);
      baseAnswerRef.current = committed;
      setSpeechText("");
      stop();
    } else {
      baseAnswerRef.current = answer;
      toggle();
    }
  }

  useEffect(() => {
    stop();
    setAnswer("");
    setSpeechText("");
    baseAnswerRef.current = "";
    textareaRef.current?.focus();
  }, [index]);

  async function submitAnswer(userAnswer) {
    setSubmitting(true);
    try {
      const res = await fetch(`${API}/evaluate`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          questionId: current.questionId,
          answer: userAnswer || "— skipped —",
          sessionId: session.sessionId,
        }),
      });
      const data = await res.json();
      return data;
    } catch {
      return null;
    } finally {
      setSubmitting(false);
    }
  }

  async function handleNext() {
    const result = await submitAnswer(answer);
    const updated = [...scores, result];
    setScores(updated);

    if (isLast) {
      onFinish(session.sessionId);
    } else {
      setIndex(index + 1);
    }
  }

  async function handleSkip() {
    const result = await submitAnswer("");
    const updated = [...scores, result];
    setScores(updated);

    if (isLast) {
      onFinish(session.sessionId);
    } else {
      setIndex(index + 1);
    }
  }

  const progress = ((index) / questions.length) * 100;

  return (
    <div className="page">
      <div className="q-container">
        <div className="q-header">
          <span className="q-role-badge">{role} · {experience}yr</span>
          <div className="q-progress-wrap">
            <div className="q-progress-bar" style={{ width: `${progress}%` }} />
          </div>
          <span className="q-counter">{index + 1} / {questions.length}</span>
        </div>

        <div className={`q-difficulty ${current.difficulty}`}>
          {current.difficulty}
        </div>

        <h2 className="q-text">{current.question}</h2>

        <div className="q-concepts">
          {current.requiredConcepts?.map((c) => (
            <span key={c} className="concept-tag">{c}</span>
          ))}
        </div>

        <div className="q-answer-wrap">
          <textarea
            ref={textareaRef}
            className={`q-answer${listening ? " listening" : ""}`}
            placeholder={listening ? "Listening... speak your answer" : "Type your answer or use the mic below..."}
            value={displayAnswer}
            onChange={(e) => {
              setAnswer(e.target.value);
              baseAnswerRef.current = e.target.value;
            }}
            disabled={submitting || listening}
          />
          <button
            className={`mic-btn${listening ? " active" : ""}${!supported ? " unavailable" : ""}`}
            onClick={handleMicToggle}
            disabled={submitting || !supported}
            title={!supported ? "Speech not supported in this browser" : listening ? "Stop recording" : "Start voice input"}
          >
            {listening ? <><span className="mic-dot" />Recording — click to stop</> : <>🎙 Speak answer</>}
          </button>
          <span className="char-count">{displayAnswer.length}</span>
        </div>

        <div className="q-actions">
          <button className="skip-btn" onClick={handleSkip} disabled={submitting}>
            Skip
          </button>
          <button className="next-btn" onClick={handleNext} disabled={submitting || !answer.trim()}>
            {submitting
              ? "Scoring..."
              : isLast
              ? "Finish Interview →"
              : "Next Question →"}
          </button>
        </div>

        {submitting && (
          <div className="scoring-indicator" style={{ marginTop: "1rem" }}>
            <div className="dot" />
            Scoring with AI embeddings...
          </div>
        )}
      </div>
    </div>
  );
}

function ResultCard({ result, index }) {
  const [open, setOpen] = useState(false);

  return (
    <div className="result-card">
      <div className="result-header" onClick={() => setOpen(!open)}>
        <div style={{ flex: 1 }}>
          <div className="result-num">Question {index + 1}</div>
          <div className="result-question">{result.questionText}</div>
        </div>
        <div className={`score-badge ${scoreClass(result.finalScore)}`}>
          {pct(result.finalScore)}
        </div>
        <span className={`chevron ${open ? "open" : ""}`}>▼</span>
      </div>

      {open && (
        <div className="result-body">
          <div className="score-breakdown">
            <div className="breakdown-item">
              <span className="breakdown-val">{pct(result.finalScore)}</span>
              <span className="breakdown-lbl">Final Score</span>
            </div>
            {result.similarityScore != null && (
              <div className="breakdown-item">
                <span className="breakdown-val">{pct(result.similarityScore)}</span>
                <span className="breakdown-lbl">Semantic Sim.</span>
              </div>
            )}
            <div className="breakdown-item">
              <span className="breakdown-val">{pct(result.requiredConceptScore)}</span>
              <span className="breakdown-lbl">Concepts Hit</span>
            </div>
            <div className="breakdown-item">
              <span className="breakdown-val">{pct(result.advancedBonus)}</span>
              <span className="breakdown-lbl">Advanced</span>
            </div>
          </div>

          {result.matchedRequired?.length > 0 && (
            <div className="matched-concepts">
              {result.matchedRequired.map((c) => (
                <span key={c} className="concept-tag">{c}</span>
              ))}
            </div>
          )}

          <div className="answer-section">
            <div className="answer-label">Your Answer</div>
            <div className="answer-text">{result.userAnswer}</div>
          </div>

          <div className="answer-section">
            <div className="answer-label">Ideal Answer</div>
            <div className="answer-text ideal-text">{result.idealAnswer}</div>
          </div>
        </div>
      )}
    </div>
  );
}

function SummaryPage({ sessionId, role, experience, onRetry }) {
  const [summary, setSummary] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function load() {
      try {
        const res = await fetch(`${API}/session/${sessionId}/summary`);
        const data = await res.json();
        setSummary(data);
      } catch {
        alert("Failed to load summary.");
      } finally {
        setLoading(false);
      }
    }
    load();
  }, [sessionId]);

  if (loading) {
    return (
      <div className="page">
        <div className="loading-wrap">
          <div className="spinner" />
          <span className="loading-text">Loading your results...</span>
        </div>
      </div>
    );
  }

  if (!summary) return null;

  const gradeColor = { A: "#47ffce", B: "#e8ff47", C: "#ffa347", D: "#ff7047", F: "#ff4747" };

  return (
    <div className="page" style={{ justifyContent: "flex-start", paddingTop: "4rem" }}>
      <div className="summary-container">
        <div className="summary-hero">
          <div className="grade-ring" style={{ borderColor: gradeColor[summary.grade] }}>
            <span className="grade-letter" style={{ color: gradeColor[summary.grade] }}>
              {summary.grade}
            </span>
          </div>
          <div className="summary-score">{pct(summary.totalScore)} overall score</div>
          <h1 className="summary-title">{gradeMessage(summary.grade)}</h1>
          <div className="summary-meta">
            {role} · {experience} yrs · {summary.totalQuestions} questions
          </div>
        </div>

        <div className="results-list">
          {summary.results.map((r, i) => (
            <ResultCard key={i} result={r} index={i} />
          ))}
        </div>

        <button className="retry-btn" onClick={onRetry}>
          Start New Interview
        </button>
      </div>
    </div>
  );
}

// ── App root ──────────────────────────────────────────────────────────────────

export default function App() {
  const [screen, setScreen] = useState("setup"); // setup | question | summary
  const [session, setSession] = useState(null);
  const [meta, setMeta] = useState({ role: "", experience: "" });
  const [sessionId, setSessionId] = useState(null);

  function handleStart(sessionData, role, experience) {
    setSession(sessionData);
    setMeta({ role, experience });
    setScreen("question");
  }

  function handleFinish(sid) {
    setSessionId(sid);
    setScreen("summary");
  }

  function handleRetry() {
    setSession(null);
    setSessionId(null);
    setScreen("setup");
  }

  return (
    <>
      <style>{css}</style>
      <div className="noise" />
      {screen === "setup" && <SetupPage onStart={handleStart} />}
      {screen === "question" && (
        <QuestionPage
          session={session}
          role={meta.role}
          experience={meta.experience}
          onFinish={handleFinish}
        />
      )}
      {screen === "summary" && (
        <SummaryPage
          sessionId={sessionId}
          role={meta.role}
          experience={meta.experience}
          onRetry={handleRetry}
        />
      )}
    </>
  );
}