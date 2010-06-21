from google.appengine.ext import webapp
from google.appengine.ext.webapp.util import run_wsgi_app
from django.utils import simplejson

def singleton(cls):
    instance_container = []
    def getinstance():
        if not len(instance_container):
            instance_container.append(cls())
        return instance_container[0]
    return getinstance
# A singleton class wrapping the game state - perhaps worth persisting it
@singleton
class State:
    max_player_id = 0
    max_game_id = 0
    players = {}
    games = {}

class GameController(webapp.RequestHandler):
    def get(self, game_id):
        game_id = int(game_id)
        state = State()
        self.response.headers['Content-Type'] = 'application/json'
        if game_id <= state.max_game_id:
            self.response.out.write(simplejson.dumps(state.games[game_id]))
        else:
            self.error(404)
            self.response.out.write('No such game')
    def post(self, game_id):
        game_id = int(game_id)
        state = State()
        if game_id <= state.max_game_id:
            self.response.headers['Content-Type'] = 'application/json'
            player_id = int(self.request.get('player_id'))
            if player_id <= state.max_player_id:
                state.games[game_id]['players'].append(state.players[player_id])
                self.response.out.write(simplejson.dumps(state.games[game_id]))
            else:
                self.error(412)
                self.response.out.write('No such player_id')
        else:
            self.error(404)
            self.response.out.write('No such game')

class PlayerController(webapp.RequestHandler):
    def get(self, player_id):
        player_id = int(player_id)
        state = State()
        self.response.headers['Content-Type'] = 'application/json'
        if player_id <= state.max_player_id:
            self.response.out.write(simplejson.dumps(state.players[player_id]))
        else:
            self.error(404)
            self.response.out.write('No such player')
    def post(self, player_id):
        player_id = int(player_id)
        state = State()
        if player_id <= state.max_player_id:
            self.response.headers['Content-Type'] = 'application/json'
            side = int(self.request.get('side'))
            state.players[player_id]['side'] = side
            self.response.out.write(simplejson.dumps(state.players[player_id]))
        else:
            self.error(404)
            self.response.write('No such player')
        

class RegisterController(webapp.RequestHandler):
    def get(self):
        state = State()
        state.max_player_id += 1
        state.players[state.max_player_id] = {"id":state.max_player_id}
        self.response.headers['Content-Type'] = 'application/json'
        self.response.out.write(simplejson.dumps(state.players[state.max_player_id]))

class StartController(webapp.RequestHandler):
    def get(self):
        state = State()
        state.max_game_id += 1
        state.games[state.max_game_id] = {"id":state.max_game_id,"players":[]}
        self.response.headers['Content-Type'] = 'application/json'
        self.response.out.write(simplejson.dumps(state.games[state.max_game_id]))

application = webapp.WSGIApplication(
                                     [
                                        ('/games/(\d+)', GameController),
                                        ('/players/(\d+)', PlayerController),
                                        ('/register', RegisterController),
                                        ('/start', StartController),
                                     ],
                                     debug=True)

def main():
    run_wsgi_app(application)

if __name__ == "__main__":
    main()
