"""
One-Click Launcher for AI Talent Screening & Hybrid Search Engine
"""
import os
import sys
import webbrowser
import uvicorn

if __name__ == "__main__":
    port = int(os.getenv("PORT", 8000))
    url = f"http://localhost:{port}"
    print("\n" + "=" * 60)
    print("🚀 AI TALENT SCREENING & HYBRID SEARCH ENGINE")
    print(f"📡 Web Dashboard UI:  {url}")
    print(f"📖 Swagger API Docs:  {url}/docs")
    print("=" * 60 + "\n")

    try:
        webbrowser.open(url)
    except Exception:
        pass

    uvicorn.run("app.main:app", host="0.0.0.0", port=port, reload=True)
