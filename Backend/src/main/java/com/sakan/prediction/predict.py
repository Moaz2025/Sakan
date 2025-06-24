import joblib
import pandas as pd
import json
import sys
import os

input_json = sys.argv[1]
new_data = json.loads(input_json)

base_dir = os.path.dirname(os.path.abspath(__file__))

model_path = os.path.join(base_dir, "model.pkl")
model = joblib.load(model_path)

X_new = pd.DataFrame([new_data])

prediction = model.predict(X_new)

print(prediction[0])
