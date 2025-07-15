import os
import gc
import torch
import whisper
import nltk
import json
import re
from flask import Flask, request, jsonify
from transformers import AutoTokenizer, AutoModelForCausalLM, pipeline
from werkzeug.utils import secure_filename

# Download NLTK resources
nltk.download('punkt', quiet=True)
from nltk.tokenize import sent_tokenize

# Configuration
os.environ["HF_HOME"] = "D:/huggingface"
device = "cuda" if torch.cuda.is_available() else "cpu"
UPLOAD_FOLDER = "uploads"
ALLOWED_AUDIO_EXTENSIONS = {'mp3', 'wav', 'ogg', 'm4a', 'flac', 'aac', 'wma', 'webm', 'amr', 'opus', '3gp'}

app = Flask(__name__)
app.config['UPLOAD_FOLDER'] = UPLOAD_FOLDER
os.makedirs(UPLOAD_FOLDER, exist_ok=True)

# Load LLaMA model
model_path = r"D:/huggingface/hub/models--meta-llama--Llama-3.2-1B-Instruct/snapshots/9213176726f574b556790deb65791e0c5aa438b6"
tokenizer = AutoTokenizer.from_pretrained(model_path)
model = AutoModelForCausalLM.from_pretrained(
    model_path,
    device_map="auto",
    torch_dtype=torch.float16 if torch.cuda.is_available() else torch.float32
)
pipe = pipeline("text-generation", model=model, tokenizer=tokenizer)

def allowed_audio_file(filename):
    return '.' in filename and filename.rsplit('.', 1)[1].lower() in ALLOWED_AUDIO_EXTENSIONS

@app.route('/speech-to-text', methods=['POST'])
def speech_to_text():
    if 'audio' not in request.files:
        return jsonify({'error': 'Audio file is required'}), 400

    file = request.files['audio']
    filename = secure_filename(file.filename)
    audio_path = os.path.join(app.config['UPLOAD_FOLDER'], filename)
    file.save(audio_path)

    model_whisper = whisper.load_model("base").to(device)
    transcription = model_whisper.transcribe(audio_path, fp16=False)
    text = transcription.get("text", "").strip()

    del model_whisper
    gc.collect()
    if torch.cuda.is_available():
        torch.cuda.empty_cache()

    return jsonify({'text': text})

def fix_grammar(sentence):
    system_prompt = """You are a professional English grammar corrector. 
    Correct the grammar in the following sentence, paying special attention to:
    1. Verb tense (e.g., 'go' → 'went' for past actions)
    2. Articles (a/an/the)
    3. Prepositions
    
    Return ONLY the corrected sentence with no additional text."""

    user_prompt = f"Original sentence: {sentence}"
    combined_prompt = f"{system_prompt}\n\n{user_prompt}\n\nCorrected sentence:"

    response = pipe(
        combined_prompt,
        max_new_tokens=100,
        temperature=0.1,
        do_sample=False,
        return_full_text=False
    )[0]['generated_text']

    corrected = response.strip()
    if "\n" in corrected:
        corrected = corrected.split("\n")[0].strip()

    corrected = re.sub(r"^(Corrected sentence:|Correction:)", "", corrected, flags=re.IGNORECASE).strip()
    return corrected

def find_differences(original, corrected):
    original_words = original.split()
    corrected_words = corrected.split()
    differences = []

    for i in range(min(len(original_words), len(corrected_words))):
        if original_words[i] != corrected_words[i]:
            differences.append((original_words[i], corrected_words[i]))

    if len(original_words) < len(corrected_words):
        for i in range(len(original_words), len(corrected_words)):
            differences.append(("", corrected_words[i]))
    elif len(original_words) > len(corrected_words):
        for i in range(len(corrected_words), len(original_words)):
            differences.append((original_words[i], ""))

    return differences

def convert_bullet_points_to_paragraph(text):
    cleaned_text = text.strip()
    bullet_points = re.findall(r"[-•]\s*(.*?)(?=\n[-•]|\n\n|$)", cleaned_text, re.DOTALL)

    if not bullet_points:
        return re.sub(r"\s+", " ", cleaned_text).strip()

    sentences = []
    for point in bullet_points:
        clean_point = re.sub(r"\s+", " ", point.strip())
        if clean_point:
            if not clean_point.endswith('.'):
                clean_point += '.'
            sentences.append(clean_point)

    paragraph = " ".join(sentences)
    return paragraph

def generate_paragraph_reason(original, corrected):
    differences = find_differences(original, corrected)
    if not differences:
        return "Kalimat sudah benar."

    differences_text = "\n".join([f"- '{old}' → '{new}'" for old, new in differences])
    system_prompt = """You are an English grammar expert.
Explain the grammar corrections between the original and corrected sentence briefly.
Only explain the words that changed. Be simple and avoid repeating the same explanation format.
Use a bullet point (-) for each change."""

    user_prompt = f"""Original: {original}
Corrected: {corrected}
Changed words:
{differences_text}

Explanation:"""

    combined_prompt = f"{system_prompt}\n\n{user_prompt}"

    response = pipe(
        combined_prompt,
        max_new_tokens=200,
        temperature=0.3,
        do_sample=False,
        return_full_text=False
    )[0]['generated_text']

    explanation = response.strip()
    explanation = re.sub(r"^(Corrections:|Explanation:|Changes:)", "", explanation, flags=re.IGNORECASE).strip()
    return explanation

def normalize_sentence(s):
    return re.sub(r'[.?!]*$', '', s.strip().lower())

@app.route('/analyze', methods=['POST'])
def analyze_text():
    data = request.form
    text = data.get('text', '').strip()

    if not text:
        return jsonify({'error': 'Text is required'}), 400

    sentences = sent_tokenize(text)
    corrected_sentences = []
    explanations = []

    for sentence in sentences:
        corrected = fix_grammar(sentence)

        if normalize_sentence(sentence) == normalize_sentence(corrected):
            explanation = "Sentence already correct."
        else:
            explanation = generate_paragraph_reason(sentence, corrected)

        corrected_sentences.append(corrected)
        explanations.append(explanation)

    corrected_paragraph = " ".join(corrected_sentences)
    analysis_results = []

    for idx, sentence in enumerate(sentences):
        analysis_results.append({
            "original": sentence,
            "corrected": corrected_sentences[idx],
            "reason": explanations[idx]
        })

    return jsonify({
            "corrected_paragraph": corrected_paragraph,
            "grammar_analysis": analysis_results
    })

if __name__ == '__main__':
    app.run(debug=True, port=5051)