from flask import Flask, request, jsonify
import joblib

app = Flask(__name__)

# Load trained artifacts
vectorizer = joblib.load("vectorizer (1).pkl")
model = joblib.load("financial_classifier_model (1).pkl")

@app.route("/predict", methods=["POST"])
def predict():
    data = request.get_json()

    if not data or "text" not in data:
        return jsonify({"error": "Missing 'text' field"}), 400

    text = data["text"]

    # 1️⃣ Transform text → features
    X = vectorizer.transform([text])

    # 2️⃣ Predict category
    category = model.predict(X)[0]

    # 3️⃣ Confidence (optional but recommended)
    if hasattr(model, "predict_proba"):
        confidence = float(model.predict_proba(X).max())
    else:
        confidence = None

    return jsonify({
        "category": category,
        "confidence": confidence
    })

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000)
