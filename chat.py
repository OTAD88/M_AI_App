import os
import socket
import requests
from bs4 import BeautifulSoup
from datetime import datetime

def is_online():
    try:
        socket.create_connection(("1.1.1.1", 53), timeout=1.5)
        return True
    except OSError:
        return False

def fetch_web_results(query):
    try:
        url = "https://html.duckduckgo.com/html/"
        headers = {"User-Agent": "Mozilla/5.0"}
        response = requests.post(url, data={"q": query}, headers=headers, timeout=4)
        soup = BeautifulSoup(response.text, "html.parser")
        snippets = soup.find_all("a", class_="result__snippet")
        if not snippets:
            return ""
        return "\n".join([f"- {s.get_text().strip()}" for s in snippets[:3]])
    except Exception:
        return ""

def save_clean_fact(topic, fact_text):
    if not fact_text.strip():
        return
    timestamp = datetime.now().strftime("%Y-%m-%d")
    entry = f"\n• [{timestamp}] ({topic}): {fact_text.strip()}\n"
    with open("knowledge.txt", "a", encoding="utf-8") as f:
        f.write(entry)

def load_knowledge():
    if os.path.exists("knowledge.txt"):
        with open("knowledge.txt", "r", encoding="utf-8") as f:
            return f.read()
    return ""

print("==========================================")
print("  المساعد الذكي M-AI (النسخة الصريحة والواقعية) ")
print("==========================================")

history = []

while True:
    user_input = input("\nأنت: ").strip()
    if user_input.lower() in ['exit', 'quit', 'خروج']:
        break

    extra_knowledge = load_knowledge()
    online = is_online()
    web_context = fetch_web_results(user_input) if online else ""

    # تعريف الشخصية الصارمة والواقعية
    system_prompt = f"""
    أنت المساعد الذكي الخاص بـ (مليون).
    
    === قواعد الشخصية والأسلوب ===
    1. الصراحة والواقعية المطلقة: يمنع المجاملة أو "التسليك" تماماً. إذا كانت الأفكار أو الأكواد أو الخطط المطروحة غير منطقية أو متوقعة الفشل، صرّح بذلك فوراً وبيّن نقاط الخلل والمخاطر بوضوح ودون مواربة.
    2. البشاشة والود: كن بشوشاً، ولطيفاً، وإيجابياً في أسلوبك، ولكن لا تدع البشاشة تجعلك تخفي الحقيقة أو تجامل على حساب الواقع.
    3. البناء والحلول: عندما تنتقد خطة أو توضح نقطة ضعف، قدم فوراً البديل العلمي والعملي الصحيح.
    4. ممنوع الاعتذار: لا تستخدم عبارات الأسف والاعتذار الفارغة ("أعتذر"، "بصفتي ذكاء اصطناعي"). ادخل في صلب الموضوع مباشرة.
    
    --- الذاكرة الدائمة للنظام ---
    {extra_knowledge}
    """

    if web_context:
        system_prompt += f"\n--- معلومات أونلاين لحظية ---\n{web_context}"

    history.append({"role": "user", "content": user_input})
    recent_messages = [{"role": "system", "content": system_prompt}] + history[-4:]

    payload = {
        "messages": recent_messages,
        "temperature": 0.3,
        "max_tokens": 800
    }

    try:
        response = requests.post("http://127.0.0.1:8080/v1/chat/completions", json=payload)
        reply = response.json()['choices'][0]['message']['content']
        print(f"\nالذكاء الاصطناعي:\n{reply}")
        history.append({"role": "assistant", "content": reply})

        # تلخيص وحفظ الحقائق أونلاين
        if online and web_context:
            summarize_payload = {
                "messages": [
                    {"role": "system", "content": "لخص المعلومة الجديدة الجوهرية من هذا النص في سطر واحد مفيد فقط:"},
                    {"role": "user", "content": web_context}
                ],
                "temperature": 0.2,
                "max_tokens": 100
            }
            sum_res = requests.post("http://127.0.0.1:8080/v1/chat/completions", json=summarize_payload)
            clean_fact = sum_res.json()['choices'][0]['message']['content']
            save_clean_fact(user_input, clean_fact)

    except Exception as e:
        print("خطأ: تأكد من تشغيل السيرفر المحلي عبر الأمر llama-server", e)
