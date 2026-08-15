"""
*****************************************************************************
* Kevin Randolph
* DAT 340
* 7/31/26
* 
* This file implements a fully interactive MongoDB dashboard for Grazioso
* Salvare. The application connects to the Austin Animal Center Outcomes
* dataset using a custom CRUD Python module and allows users to filter
* dogs by rescue type. The dashboard dynamically updates a data table,
* breed breakdown chart and geolocation map based on user selections.
*
* The application follows an MVC-style structure where MongoDB serves
* as the model, Dash components provide the view and callback functions
* act as the controller to manage user interaction and data updates.
*
* Enhanced for CS 499 Milestone Four:
*   1. Database credentials are now read from environment variables instead
*      of being hardcoded in this file
*   2. The AnimalShelter connection is wrapped to handle the new
*      DatabaseConnectionError raised by the enhanced CRUD module
*   3. Filtering is no longer limited to four fixed presets. Breed, sex
*      and age range can now be combined independently with (or instead of)
*      a rescue type preset
*   4. A new breed summary table uses a MongoDB aggregation pipeline
*      ($match + $group + $sort) to show grouped counts rather than only
*      ever showing a flat list of matching documents
*   5. update_map() now looks up columns by name first, falling back to
*      positional access only if the named columns aren't present. This
*      reverses the original order where positional access was tried
*      first and name based lookup was only the fallback
*****************************************************************************
"""

# Setup the Jupyter version of Dash
from jupyter_dash import JupyterDash

# Dashboard components
import dash_leaflet as dl
from dash import dcc, html
from dash import dash_table
from dash.dependencies import Input, Output

import plotly.express as px
import pandas as pd
import base64
import os
import sys

JupyterDash.infer_jupyter_proxy_config()

# ---------------------------------------
# Import CRUD module 
# ---------------------------------------
from CRUD_Python_Module import AnimalShelter, DatabaseConnectionError

# ---------------------------------------
# Database connection 
# ---------------------------------------
# enhancement: credentials are now read from environment variables instead
# of being hardcoded as plain text strings in this file. This matches the
# secure pattern the CRUD module's constructor already expected but wasn't
# actually followed by this script before
username = os.environ.get("AAC_DB_USERNAME")
password = os.environ.get("AAC_DB_PASSWORD")

if not username or not password:
    # enhancement: fail clearly and immediately if credentials aren't set
    # rather than passing None/empty values into the connection attempt
    print(
        "Missing database credentials. Please set the AAC_DB_USERNAME and "
        "AAC_DB_PASSWORD environment variables before running this app."
    )
    sys.exit(1)

try:
    # enhancement: the AnimalShelter constructor can now raise
    # DatabaseConnectionError instead of silently continuing with a
    # broken connection, so that failure is handled explicitly here
    shelter = AnimalShelter(username, password)
except DatabaseConnectionError as e:
    print(f"Failed to start dashboard: {e}")
    sys.exit(1)

# ---------------------------------------
# Load Grazioso Salvare logo
# ---------------------------------------
image_filename = image_filename = "Grazioso Salvare Logo.png"

encoded_logo = base64.b64encode(
    open(image_filename, "rb").read()
).decode("utf-8")

# ---------------------------------------
# Filter queries/controller logic
# ---------------------------------------

# enhancement: preset dictionaries are kept as convenient starting points,
# but build_query() no longer treats them as the only option. Breed, sex
# and age range can now be layered on top of a preset, or used entirely on
# their own with RESET as the base
_PRESET_QUERIES = {
    "RESET": {},
    "WATER": {
        "animal_type": "Dog",
        "breed": {"$in": ["Labrador Retriever Mix", "Chesapeake Bay Retriever", "Newfoundland"]},
        "sex_upon_outcome": "Intact Female",
        "age_upon_outcome_in_weeks": {"$gte": 26, "$lte": 156}
    },
    "MOUNTAIN": {
        "animal_type": "Dog",
        "breed": {"$in": ["German Shepherd", "Alaskan Malamute", "Old English Sheepdog", "Siberian Husky", "Rottweiler"]},
        "sex_upon_outcome": "Intact Male",
        "age_upon_outcome_in_weeks": {"$gte": 26, "$lte": 156}
    },
    "DISASTER": {
        "animal_type": "Dog",
        "breed": {"$in": ["Doberman Pinscher", "German Shepherd", "Golden Retriever", "Bloodhound", "Rottweiler"]},
        "sex_upon_outcome": "Intact Male",
        "age_upon_outcome_in_weeks": {"$gte": 20, "$lte": 300}
    },
}


def build_query(filter_type, breed=None, sex=None, min_age=None, max_age=None):
    """
    Build a MongoDB query dict.

    enhancement: filter_type still selects one of the original rescue-type
    presets as a convenient starting point, but breed, sex, and an age range
    can now each be supplied independently and will override or add to
    whatever the preset specified. This lets a user combine a preset with
    a more specific breed, or ignore presets entirely and build a fully
    custom query from RESET.
    """
    query = dict(_PRESET_QUERIES.get(filter_type, {"animal_type": "Dog"}))

    # enhancement: breed is applied as a case insensitive partial match
    # rather than requiring an exact name from a fixed list
    if breed:
        query["breed"] = {"$regex": breed, "$options": "i"}

    if sex:
        query["sex_upon_outcome"] = sex

    # enhancement: age range replaces the presets fixed range only when
    # the user has actually entered a value, so partial overrides work too
    if min_age is not None or max_age is not None:
        age_filter = {}
        if min_age is not None:
            age_filter["$gte"] = min_age
        if max_age is not None:
            age_filter["$lte"] = max_age
        query["age_upon_outcome_in_weeks"] = age_filter

    return query


def fetch_dataframe(filter_type, breed=None, sex=None, min_age=None, max_age=None):
    query = build_query(filter_type, breed=breed, sex=sex, min_age=min_age, max_age=max_age)
    records = shelter.read(query)

    df = pd.DataFrame.from_records(records)

    # Handle empty result sets
    if df.empty:
        return df

    # Drop _id, ObjectId breaks DataTable
    if "_id" in df.columns:
        df.drop(columns=["_id"], inplace=True)

    return df


# enhancement: uses a MongoDB aggregation pipeline instead of a flat find()
# query so the dashboard can show grouped/summarized data (breed counts)
# rather than only ever showing individual matching documents
def get_breed_summary(filter_type, breed=None, sex=None, min_age=None, max_age=None):
    match_query = build_query(filter_type, breed=breed, sex=sex, min_age=min_age, max_age=max_age)

    pipeline = [
        {"$match": match_query},
        {"$group": {"_id": "$breed", "count": {"$sum": 1}}},
        {"$sort": {"count": -1}},
    ]

    if shelter.collection is None:
        return pd.DataFrame(columns=["breed", "count"])

    try:
        results = list(shelter.collection.aggregate(pipeline))
    except Exception as e:
        print(f"Aggregation error: {e}")
        return pd.DataFrame(columns=["breed", "count"])

    if not results:
        return pd.DataFrame(columns=["breed", "count"])

    summary_df = pd.DataFrame(results).rename(columns={"_id": "breed"})
    return summary_df


# Initial load: RESET view
df_initial = fetch_dataframe("RESET")

# ---------------------------------------
# App layout/view
# ---------------------------------------
app = JupyterDash(__name__)

app.layout = html.Div([
    html.Div(id="hidden-div", style={"display": "none"}),

    # Header row with logo/identifier
    html.Div(
        style={"display": "flex", "alignItems": "center", "gap": "20px"},
        children=[
            html.Div(
                children=[
                    html.H1("Grazioso Salvare Dashboard"),
                    html.H3("Unique Identifier: Kevin Randolph")
                ]
            ),
            html.Div(
                children=[
                    html.Img(
                        src=f"data:image/png;base64,{encoded_logo}" if encoded_logo else "",
                        style={"height": "90px"} 
                    ) if encoded_logo else html.Div("Logo file not found. Check the path: code_files/Grazioso Salvare Logo.png")
                ]
            ),
        ]
    ),

    html.Hr(),

    # Filter controls
    html.Div(
        children=[
            html.H3("Rescue Type Filter"),
            dcc.RadioItems(
                id="filter-type",
                options=[
                    {"label": "Reset", "value": "RESET"},
                    {"label": "Water Rescue", "value": "WATER"},
                    {"label": "Mountain or Wilderness Rescue", "value": "MOUNTAIN"},
                    {"label": "Disaster or Individual Tracking", "value": "DISASTER"},
                ],
                value="RESET",
                labelStyle={"display": "block"}
            ),

            # enhancement: independent filter controls that can be combined
            # with the preset above instead of being locked to one of the
            # four fixed rescue type queries
            html.H3("Additional Filters (optional, combine with preset above)", style={"marginTop": "15px"}),
            html.Div(
                style={"display": "flex", "gap": "20px", "flexWrap": "wrap"},
                children=[
                    html.Div([
                        html.Label("Breed contains:"),
                        dcc.Input(id="breed-filter", type="text", placeholder="e.g. Retriever")
                    ]),
                    html.Div([
                        html.Label("Sex:"),
                        dcc.Dropdown(
                            id="sex-filter",
                            options=[
                                {"label": "Any", "value": ""},
                                {"label": "Intact Male", "value": "Intact Male"},
                                {"label": "Intact Female", "value": "Intact Female"},
                                {"label": "Neutered Male", "value": "Neutered Male"},
                                {"label": "Spayed Female", "value": "Spayed Female"},
                            ],
                            value="",
                            style={"width": "180px"}
                        )
                    ]),
                    html.Div([
                        html.Label("Min age (weeks):"),
                        dcc.Input(id="min-age-filter", type="number", placeholder="e.g. 20")
                    ]),
                    html.Div([
                        html.Label("Max age (weeks):"),
                        dcc.Input(id="max-age-filter", type="number", placeholder="e.g. 300")
                    ]),
                ]
            ),
        ]
    ),

    html.Hr(),

    # Data table
    dash_table.DataTable(
        id="datatable-id",
        columns=[{"name": i, "id": i, "deletable": False, "selectable": True} for i in df_initial.columns],
        data=df_initial.to_dict("records"),

        row_selectable="single",
        selected_rows=[0],

        page_size=10,
        sort_action="native",
        filter_action="native",
        column_selectable="single",

        style_table={"overflowX": "auto"},
        style_cell={"textAlign": "left", "minWidth": "120px", "width": "120px", "maxWidth": "220px"},
        style_header={"fontWeight": "bold"},
    ),

    html.Br(),
    html.Hr(),

    # Chart + Map 
    html.Div(
        className="row",
        style={"display": "flex", "gap": "20px"},
        children=[
            html.Div(id="graph-id", className="col s12 m6"),
            html.Div(id="map-id", className="col s12 m6"),
        ]
    ),

    html.Br(),
    html.Hr(),

    # enhancement: new section showing the aggregation-based breed summary
    # a grouped/summarized view rather than a flat list of matching records
    html.Div(
        children=[
            html.H3("Breed Summary (Aggregated Counts)"),
            html.Div(id="breed-summary-table")
        ]
    ),
])


# ---------------------------------------
# Callbacks/controller
# ---------------------------------------

# Update table based on filter selection
# enhancement: now takes the additional filter controls as inputs and
# passes them through to fetch_dataframe() alongside the preset
@app.callback(
    Output("datatable-id", "data"),
    Output("datatable-id", "columns"),
    Output("datatable-id", "selected_rows"),
    Input("filter-type", "value"),
    Input("breed-filter", "value"),
    Input("sex-filter", "value"),
    Input("min-age-filter", "value"),
    Input("max-age-filter", "value"),
)
def update_dashboard(filter_type, breed, sex, min_age, max_age):
    df = fetch_dataframe(
        filter_type,
        breed=breed if breed else None,
        sex=sex if sex else None,
        min_age=min_age,
        max_age=max_age,
    )

    # If no rows, return empty table
    if df.empty:
        return [], [], []

    columns = [{"name": i, "id": i, "deletable": False, "selectable": True} for i in df.columns]
    data = df.to_dict("records")

    # Default select the first row so map always has something to show
    return data, columns, [0]


# Breed pie chart from table data 
@app.callback(
    Output("graph-id", "children"),
    Input("datatable-id", "derived_virtual_data")
)
def update_graphs(viewData):
    if viewData is None or len(viewData) == 0:
        return [html.Div("No data available for chart.")]

    dff = pd.DataFrame.from_dict(viewData)

    if "breed" not in dff.columns:
        return [html.Div("Column 'breed' not found for chart.")]

    fig = px.pie(dff, names="breed", title="Breed Breakdown (Filtered Results)")
    return [dcc.Graph(figure=fig)]


# Highlight selected columns
@app.callback(
    Output("datatable-id", "style_data_conditional"),
    Input("datatable-id", "selected_columns")
)
def update_styles(selected_columns):
    if not selected_columns:
        return []
    return [{
        "if": {"column_id": i},
        "backgroundColor": "#D2F3FF"
    } for i in selected_columns]


# enhancement: new callback populating the aggregation based breed summary
# table whenever any filter changes, using get_breed_summary() rather than
# a flat find() query
@app.callback(
    Output("breed-summary-table", "children"),
    Input("filter-type", "value"),
    Input("breed-filter", "value"),
    Input("sex-filter", "value"),
    Input("min-age-filter", "value"),
    Input("max-age-filter", "value"),
)
def update_breed_summary(filter_type, breed, sex, min_age, max_age):
    summary_df = get_breed_summary(
        filter_type,
        breed=breed if breed else None,
        sex=sex if sex else None,
        min_age=min_age,
        max_age=max_age,
    )

    if summary_df.empty:
        return html.Div("No breed summary data available for this filter.")

    return dash_table.DataTable(
        columns=[{"name": "Breed", "id": "breed"}, {"name": "Count", "id": "count"}],
        data=summary_df.to_dict("records"),
        page_size=10,
        style_cell={"textAlign": "left"},
        style_header={"fontWeight": "bold"},
    )


# Map updates from selected row
# enhancement: now looks up columns by name first (reliable regardless of
# column order) and only falls back to positional .iloc access if the
# named columns aren't present at all. This reverses the original logic,
# which tried positional access first and treated name-based lookup as
# only the fallback
@app.callback(
    Output("map-id", "children"),
    Input("datatable-id", "derived_virtual_data"),
    Input("datatable-id", "derived_virtual_selected_rows")
)
def update_map(viewData, index):
    if viewData is None or len(viewData) == 0:
        return [html.Div("No map data to display for this filter.")]

    dff = pd.DataFrame.from_dict(viewData)

    row = 0
    if index is not None and len(index) > 0:
        row = index[0]
    if row >= len(dff):
        row = 0

    # enhancement: name based lookup first, since column position can
    # shift depending on which filters were applied
    if "location_lat" in dff.columns and "location_long" in dff.columns:
        lat = dff["location_lat"].iloc[row]
        lon = dff["location_long"].iloc[row]
    else:
        # enhancement: positional access is now the fallback, only used if
        # the expected named columns are missing entirely
        try:
            lat = dff.iloc[row, 13]
            lon = dff.iloc[row, 14]
        except Exception:
            lat, lon = 30.75, -97.48

    if "breed" in dff.columns:
        breed = dff["breed"].iloc[row]
    else:
        try:
            breed = dff.iloc[row, 4]
        except Exception:
            breed = ""

    if "name" in dff.columns:
        name = dff["name"].iloc[row]
    else:
        try:
            name = dff.iloc[row, 9]
        except Exception:
            name = ""

    # convert to floats (safe fallback)
    try:
        lat = float(lat)
        lon = float(lon)
    except (TypeError, ValueError):
        lat, lon = 30.75, -97.48

    return [
        dl.Map(
            style={"width": "1000px", "height": "500px"},
            center=[30.75, -97.48],
            zoom=10,
            children=[
                dl.TileLayer(id="base-layer-id"),
                dl.Marker(
                    position=[lat, lon],
                    children=[
                        dl.Tooltip(str(breed)),
                        dl.Popup([
                            html.H1("Animal Name"),
                            html.P(str(name))
                        ])
                    ]
                )
            ]
        )
    ]


# Runs app in Codio JupyterLab mode
app.run_server(mode="jupyterlab", host="0.0.0.0", port=8051, debug=False)