import sys
from pathlib import Path

# Resolve directory paths
api_dir = Path(__file__).resolve().parent
root_dir = api_dir.parent
nlp_dir = root_dir / "ai-nlp-service"

for path in [str(nlp_dir), str(root_dir)]:
    if path not in sys.path:
        sys.path.insert(0, path)

from app.main import app
