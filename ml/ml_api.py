from flask import Flask, request, jsonify
import joblib
import os

MODEL_OK = True

app = Flask(__name__)

# Load trained artifacts with safe fallbacks
VECT_PATH = os.environ.get("NUDGER_VECT_PATH", "vectorizer (1).pkl")
MODEL_PATH = os.environ.get("NUDGER_MODEL_PATH", "financial_classifier_model (1).pkl")

vectorizer = None
model = None

try:
    vectorizer = joblib.load(VECT_PATH)
    model = joblib.load(MODEL_PATH)
except Exception as e:
    MODEL_OK = False
    print(f"[ml_api] Warning: Failed to load model/vectorizer: {e}")
    print("[ml_api] API will respond with a fallback category while you fix artifacts.")

@app.route("/predict", methods=["POST"])
def predict():
    data = request.get_json()

    if not data or "text" not in data:
        return jsonify({"error": "Missing 'text' field"}), 400

    text = data["text"]

    if not MODEL_OK or vectorizer is None or model is None:
        # Fallback path: keep pipeline alive while artifacts are fixed
        return jsonify({"category": "Miscellaneous", "confidence": 0.0})

    # 1️⃣ Transform text → features
    X = vectorizer.transform([text])

    # 2️⃣ Predict category
    category = model.predict(X)[0]

    # 3️⃣ Confidence (optional but recommended)
    confidence = None
    if hasattr(model, "predict_proba"):
        try:
            confidence = float(model.predict_proba(X).max())
        except Exception:
            confidence = None

    return jsonify({"category": category, "confidence": confidence})

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000)
